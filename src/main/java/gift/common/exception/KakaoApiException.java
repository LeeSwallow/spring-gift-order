package gift.common.exception;

import org.springframework.http.HttpStatus;

public class KakaoApiException extends RuntimeException {
    private final HttpStatus status;
    private final Integer errorCode;
    private final String errorMessage;

    public KakaoApiException(HttpStatus status, Integer errorCode, String errorMessage) {
        super("카카오 API 호출에 실패했습니다: " + errorMessage + " (Error Code: " + errorCode + ")");
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
