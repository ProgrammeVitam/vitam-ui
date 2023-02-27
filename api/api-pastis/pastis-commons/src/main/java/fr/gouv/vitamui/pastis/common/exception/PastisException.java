package fr.gouv.vitamui.pastis.common.exception;

public class PastisException extends Exception {
    public PastisException(String message) {
        super(message);
    }

    public PastisException(Throwable cause) {
        super(cause);
    }

    public PastisException(String message, Throwable cause) {
        super(message, cause);
    }
}
