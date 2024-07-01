package dev.javarush.oauth2.authorizationserver.token;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("realms/{realmId}/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> handleRequest (
            @PathVariable("realmId") String realmId,
            @RequestParam Map<String, String> query,
            HttpServletResponse response
    ) {
        var tokenRequest = new TokenRequest(
                realmId,
                query.get("client_id"),
                query.get("client_secret"),
                query.get("grant_type"),
                query.get("code"),
                query.get("redirect_uri"),
                query.get("code_verifier")
        );
        this.tokenService.verifyTokenRequest (tokenRequest);

        // Set the header
        response.setHeader("Cache-Control", "no-store");
        return this.tokenService.generateAccessTokenResponse(tokenRequest);
    }
}

@Component
class TokenCorsFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final CorsProcessor corsProcessor = new DefaultCorsProcessor();

    TokenCorsFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("TokenCorsFilter::doFilterInternal - ");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String path = (servletPath == null ? "" : servletPath) + (pathInfo == null ? "" : pathInfo);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] pathElements = path.split("/");
        if (pathElements.length == 3 && pathElements[0].equals("realms")
                && pathElements[2].equals("token")) {
            String realmId = pathElements[1];

            List<String> origins = this.tokenService.getAllowedOrigins (realmId);

            configuration.setAllowedOrigins(origins);
            configuration.setAllowedMethods(List.of("OPTIONS", "HEAD", "GET", "POST"));
            configuration.addAllowedHeader("*");
            source.registerCorsConfiguration("/**", configuration);
        }
        CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
        boolean isValid = this.corsProcessor.processRequest(corsConfiguration, request, response);
        if (!isValid || CorsUtils.isPreFlightRequest(request)) {
            return;
        }
        filterChain.doFilter(request, response);
    }
}
