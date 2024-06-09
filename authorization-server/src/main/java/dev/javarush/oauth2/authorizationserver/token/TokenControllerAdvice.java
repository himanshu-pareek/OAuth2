package dev.javarush.oauth2.authorizationserver.token;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TokenControllerAdvice {

    private ProblemDetail createProblemDetail (InvalidTokenRequestException e, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);

        problemDetail.setTitle(e.getError());
        if (e.getErrorDescription() != null) {
            problemDetail.setDetail(e.getErrorDescription());
            problemDetail.setProperty("error_description", e.getErrorDescription());
        }

        if (e.getErrorUri() != null) {
            problemDetail.setType(URI.create(e.getErrorUri()));
            problemDetail.setProperty("error_uri", e.getErrorUri());
        }

        return problemDetail;
    }

    @ExceptionHandler(InvalidTokenRequestInvalidClientException.class)
    public ResponseEntity<ProblemDetail> handleInvalidClientException (InvalidTokenRequestInvalidClientException e) {
        return ResponseEntity.of(createProblemDetail(e, HttpStatus.UNAUTHORIZED)).build();
    }

    @ExceptionHandler(InvalidTokenRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenRequestException (InvalidTokenRequestException e) {
        return ResponseEntity.of(createProblemDetail(e, HttpStatus.BAD_REQUEST)).build();
    }
}
