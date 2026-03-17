package eu.deic.url_shortener.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String shortCode, String owner) {
        super("'" + shortCode + "' is not owned by '" + owner + "'");
    }
}
