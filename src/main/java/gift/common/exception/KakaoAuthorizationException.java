package gift.common.exception;

import org.springframework.http.HttpStatus;

public class KakaoAuthorizationException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        if (errorCode.length() >= 6 && errorCode.startsWith("KOE")) {
            int codeNum = Integer.parseInt(errorCode.substring(3, 6));
            return switch (codeNum) {
                case 1, 2, 4, 5, 6, 7, 8, 201, 202, 203, 204, 205, 206, 207 -> HttpStatus.BAD_REQUEST;
                case 101, 102 -> HttpStatus.UNAUTHORIZED;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        } else if (errorCode.contains("access_denied")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (
                errorCode.contains("login_required") ||
                errorCode.contains("consent_required") ||
                errorCode.contains("interaction_required")
        ) {
            return HttpStatus.FORBIDDEN;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public KakaoAuthorizationException(String errorCode, String description) {
        super("카카오 인증에 실패했습니다: " + description + " (Error Code: " + errorCode + ")");
        this.status = mapErrorCodeToStatus(errorCode);
        this.errorCode = errorCode;
    }

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
