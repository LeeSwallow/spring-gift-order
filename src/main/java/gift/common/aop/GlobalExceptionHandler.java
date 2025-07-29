package gift.common.aop;

import gift.common.exception.*;
import gift.common.model.error.ErrorMessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.NoSuchElementException;

@RestControllerAdvice(basePackages = "gift.controller.api")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder("유효성 검사에서 오류가 발생했습니다.", HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .extractValidationErrorsFrom(e);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder("유효성 검사에서 오류가 발생했습니다.", HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .extractValidationErrorsFrom(e);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder("유효성 검사에서 오류가 발생했습니다.", HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .extractValidationErrorsFrom(e);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            UnauthorizedException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer")
                .body(builder.build().toProblemDetail());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(
            NoSuchElementException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ProblemDetail> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(
            NoResourceFoundException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.NOT_FOUND);
        ProblemDetail errorDetail = builder.build().toProblemDetail();
        errorDetail.setDetail("요청한 리소스를 찾을 수 없습니다.");
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateKeyException(
            DuplicateKeyException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.CONFLICT);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> handleResourceAccessException(
            ResourceAccessException e, HttpServletRequest request
    ) {
        if (e.getCause() instanceof SocketTimeoutException) {
            var detail = new ErrorMessageResponse.Builder(request, e, HttpStatus.REQUEST_TIMEOUT).build()
                    .toProblemDetail();
            detail.setTitle("요청 시간 초과");
            detail.setDetail("외부 서비스에 접근하는 동안 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            return new ResponseEntity<>(detail, HttpStatus.REQUEST_TIMEOUT);
        }

        var detail = new ErrorMessageResponse.Builder(request, e,  HttpStatus.SERVICE_UNAVAILABLE).build()
                .toProblemDetail();
        detail.setTitle("서비스 이용 불가");
        detail.setDetail("외부 서비스에 접근할 수 없습니다. 잠시 후 다시 시도해주세요.");
        return new ResponseEntity<>(detail, HttpStatus.SERVICE_UNAVAILABLE);
    }


    @ExceptionHandler(KakaoAuthorizationException.class)
    public ResponseEntity<ProblemDetail> handleKakaoAuthorizationException(
            KakaoAuthorizationException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, e.getStatus());
        ProblemDetail errorDetail = builder.build().toProblemDetail();
        errorDetail.setType(URI.create("https://developers.kakao.com/docs/latest/ko/kakaologin/trouble-shooting"));
        errorDetail.setTitle("카카오 인증 오류 : " + e.getErrorCode());
        return new ResponseEntity<>(errorDetail, e.getStatus());
    }

    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<ProblemDetail> handleKakaoApiException(
            KakaoApiException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, e.getStatus());
        ProblemDetail errorDetail = builder.build().toProblemDetail();
        errorDetail.setType(URI.create("https://developers.kakao.com/docs/latest/ko/rest-api/reference#error-code"));
        errorDetail.setTitle("카카오 API 오류 : " + e.getErrorCode());
        return new ResponseEntity<>(errorDetail, e.getStatus());
    }

    @ExceptionHandler(CriticalServerException.class)
    public ResponseEntity<ProblemDetail> handleCriticalServerException(
            CriticalServerException e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.INTERNAL_SERVER_ERROR);
        log.error("치명적인 서버 오류가 발생했습니다: {}", e.getMessage(), e);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleDefaultException(
            Exception e, HttpServletRequest request
    ) {
        var builder = new ErrorMessageResponse.Builder(request, e, HttpStatus.INTERNAL_SERVER_ERROR);
        log.error("예상치 못한 오류가 발생했습니다: {}", e.getMessage(), e);
        return new ResponseEntity<>(builder.build().toProblemDetail(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
