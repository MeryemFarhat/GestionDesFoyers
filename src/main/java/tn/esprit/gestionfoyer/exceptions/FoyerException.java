package tn.esprit.gestionfoyer.exceptions;

import org.springframework.http.HttpStatus;

public class FoyerException extends RuntimeException {
    private final HttpStatus status;

    private FoyerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public static FoyerException missing(String resource, Object identifier) {
        return new FoyerException(HttpStatus.NOT_FOUND, resource + " introuvable (" + identifier + ")");
    }

    public static FoyerException conflict(String message) {
        return new FoyerException(HttpStatus.CONFLICT, message);
    }
}
