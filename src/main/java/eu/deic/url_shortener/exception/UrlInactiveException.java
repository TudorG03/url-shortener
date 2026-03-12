package eu.deic.url_shortener.exception;

public class UrlInactiveException extends RuntimeException {

    public UrlInactiveException(String shortCode) {
        super("URL has been deactivated: " + shortCode);
    }
}
