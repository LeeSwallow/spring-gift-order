package gift.common.exception;

public class CriticalServerException extends RuntimeException {
    public CriticalServerException(String message) {
        super(message);
    }
    public CriticalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
