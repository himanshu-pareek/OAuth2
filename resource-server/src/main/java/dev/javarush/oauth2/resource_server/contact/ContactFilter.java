package dev.javarush.oauth2.resource_server.contact;

import dev.javarush.oauth2.resource_server.crypto.CryptoRepository;
import dev.javarush.oauth2.resource_server.crypto.InvalidJWTException;
import dev.javarush.oauth2.resource_server.crypto.JWTVerifier;
import dev.javarush.oauth2.resource_server.token.AccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class ContactFilter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    FilterRegistrationBean<ContactReadFilter> contactReadFilter() {
        var registrationBean = new FilterRegistrationBean<ContactReadFilter>();
        registrationBean.setFilter(new ContactReadFilter(this.applicationContext));
        registrationBean.addUrlPatterns("/contacts/*");
        registrationBean.setEnabled(true);
        return registrationBean;
    }

    @Bean
    FilterRegistrationBean<ContactWriteFilter> contactWriteFilter() {
        var registrationBean = new FilterRegistrationBean<ContactWriteFilter>();
        registrationBean.setFilter(new ContactWriteFilter(this.applicationContext));
        registrationBean.addUrlPatterns("/contacts/*");
        registrationBean.setEnabled(true);
        return registrationBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

class ContactReadFilter extends HttpFilter {
    private static final Logger log = LoggerFactory.getLogger(ContactReadFilter.class);
    private static final Set<String> requiredScopes = Set.of("contact.read");
    private final ApplicationContext applicationContext;
    private final AccessTokenFilterHelper helper;

    public ContactReadFilter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.helper = new AccessTokenFilterHelper(
                new HashSet<>(requiredScopes),
                this::getCryptoRepository
        );
    }

    private CryptoRepository getCryptoRepository() {
        return this.applicationContext.getBean(CryptoRepository.class);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 1. Check whether to apply the filter or not
        if (!request.getMethod().equals(RequestMethod.GET.name())) {
            chain.doFilter(request, response);
            return;
        }

        log.info("Validating request to - GET {}", request.getServletPath());
        boolean isValid = this.helper.validate(request, response);

        if (!isValid) {
            return;
        }

        // 3. Reject or Continue the request
        chain.doFilter(request, response);
    }
}

class ContactWriteFilter extends HttpFilter {
    private static final Logger log = LoggerFactory.getLogger(ContactWriteFilter.class);
    private static final Set<String> requiredScopes = Set.of("contact.read", "contact.write");
    private final ApplicationContext applicationContext;
    private final AccessTokenFilterHelper helper;

    public ContactWriteFilter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.helper = new AccessTokenFilterHelper(
                new HashSet<>(requiredScopes),
                this::getCryptoRepository
        );
    }

    private CryptoRepository getCryptoRepository() {
        return this.applicationContext.getBean(CryptoRepository.class);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 1. Check whether to apply the filter or not
        if (!request.getMethod().equals(RequestMethod.POST.name())) {
            chain.doFilter(request, response);
            return;
        }

        log.info("Validating request to - POST {}", request.getServletPath());
        boolean isValid = this.helper.validate(request, response);

        if (!isValid) {
            return;
        }

        // 3. Reject or Continue the request
        chain.doFilter(request, response);
    }
}

@FunctionalInterface
interface CryptoRepositoryRetriever {

    CryptoRepository getRepository();

}

class AccessTokenFilterHelper {

    private final Collection<String> requiredScopes;
    private final CryptoRepositoryRetriever cryptoRepositoryRetriever;

    AccessTokenFilterHelper(Collection<String> requiredScopes, CryptoRepositoryRetriever cryptoRepositoryRetriever) {
        this.requiredScopes = requiredScopes;
        this.cryptoRepositoryRetriever = cryptoRepositoryRetriever;
    }

    boolean validate (HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            errorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "token_absent",
                    "The access token is not present"
            );
            return false;
        }

        // 2. Check whether to allow the request or not
        String jwtAccessToken = authHeader.substring(7);
        AccessToken accessToken;
        try {
            accessToken = new JWTVerifier<>(jwtAccessToken, AccessToken.class)
                    .verify(this.cryptoRepositoryRetriever.getRepository().getPublicKeyEntry())
                    .getToken();
        } catch (InvalidJWTException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            errorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "invalid_token",
                    "The access token is not valid"
            );
            return false;
        }

        var tokenExpiryTime = LocalDateTime.ofEpochSecond(accessToken.expiresAt(), 0, ZoneOffset.UTC);
        if (tokenExpiryTime.isBefore(LocalDateTime.now())) {
            // Token expired
            errorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "invalid_token",
                    "The access token is expired"
            );
            return false;
        }

        HashSet<String> availableScopes = new HashSet<>(Arrays.asList(accessToken.scopes().split(" ")));
        boolean allAvailable = availableScopes.containsAll(requiredScopes);
        if (!allAvailable) {
            // Scopes are missing
            // Access Denied
            errorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "access_denied",
                    "You are not allowed to access " + request.getServletPath()
            );
            return false;
        }
        return true;
    }

    private void errorResponse(
            HttpServletResponse response,
            int status,
            String error,
            String errorDescription
    ) throws IOException {
        response.setStatus(status);
        response.addHeader("WWW-Authenticate", "Bearer realm=\"java\"");
        response.addHeader("WWW-Authenticate", "scope=\"" + String.join(" ", requiredScopes) + "\"");
        response.addHeader("WWW-Authenticate", "error=\"" + error + "\"");
        response.addHeader("WWW-Authenticate", "error_description=\"" + errorDescription + "\"");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + error + "\", \"error_description\": \"" + errorDescription + "\"}\n");
    }
}
