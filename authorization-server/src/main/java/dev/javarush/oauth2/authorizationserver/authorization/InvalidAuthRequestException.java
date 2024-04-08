package dev.javarush.oauth2.authorizationserver.authorization;

public class InvalidAuthRequestException extends RuntimeException {
    private String redirectUri;
    private String errorCode;
    private String errorDescription;

    public InvalidAuthRequestException(String message) {
        super(message);
    }

    public InvalidAuthRequestException(String redirectUri, String errorCode, String errorDescription) {
        this.redirectUri = redirectUri;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
