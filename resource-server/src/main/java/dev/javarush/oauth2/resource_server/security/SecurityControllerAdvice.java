package dev.javarush.oauth2.resource_server.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityControllerAdvice {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ProblemDetail> handleSecurityException (SecurityException exception) {
        ProblemDetail pd = ProblemDetail.forStatus(exception.getStatus());
        pd.setTitle(exception.getError());
        pd.setDetail(exception.getErrorDescription());
        pd.setProperty("error", exception.getError());
        pd.setProperty("error_description", exception.getErrorDescription());
        return ResponseEntity.of(pd)
                .header(
                        "WWW-Authenticate",
                        "Bearer realm=\"java\"",
                        "scope=\"" + String.join(" ", exception.getScopes()) + "\"",
                        "error=\"" + exception.getError() + "\"",
                        "error_description=\"" + exception.getErrorDescription() + "\""
                )
                .header("Content-Type", "application/json")
                .header("Charset", "UTF-8")
                .build();
    }

}
