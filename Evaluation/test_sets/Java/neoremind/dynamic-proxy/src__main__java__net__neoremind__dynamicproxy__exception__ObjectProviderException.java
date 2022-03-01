package net.neoremind.dynamicproxy.exception;

public class ObjectProviderException extends RuntimeException {

    private static final long serialVersionUID = 3222573813262320183L;

    public ObjectProviderException() {
    }

    public ObjectProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectProviderException(String message) {
        super(message);
    }

    public ObjectProviderException(Throwable cause) {
        super(cause);
    }

}
