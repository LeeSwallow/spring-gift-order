package gift.common.exception;

import org.springframework.http.HttpStatus;

public class KakaoAuthorizationException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public KakaoAuthorizationException(HttpStatus status,String errorCode, String description) {
        super("카카오 인증에 실패했습니다: " + description + " (Error Code: " + errorCode + ")");
        this.status = status;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
    public HttpStatus getStatus() {
        return status;
    }
}
