package gift.common.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessageResponse (
    String message,
    int status,
    String error,
    String path,
    List<String> stackTrace,
    LocalDateTime timestamp,
    List<ValidationError> validationErrors
) {

    public static class Builder {
        private final String message;
        private final int status;
        private String error;
        private String path = null;
        private LocalDateTime timestamp;
        private List<String> stackTrace = null;
        private List<ValidationError> validationErrors = null;
        private boolean showStackTrace = false;

        public Builder(String message, HttpStatus status) {
            this.message = message;
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.timestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        }

        public Builder(HttpServletRequest request, Exception exception, HttpStatus status) {
            this.message = exception.getMessage();
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.path = request.getRequestURI();
            this.timestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
            this.stackTrace = new ArrayList<>();

            for (StackTraceElement element : exception.getStackTrace()) {
                stackTrace.add(element.toString());
            }
        }

        public Builder showStackTrace() {
            this.showStackTrace = true;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public Builder stackTrace(List<String> stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder validationErrors(List<ValidationError> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public Builder extractValidationErrorsFrom(HandlerMethodValidationException e) {
            this.validationErrors = new ArrayList<>();
            e.getValueResults().forEach(result -> {
                validationErrors.addAll(ValidationError.from(result));
            });
            e.getBeanResults().forEach(beanResult -> {
                beanResult.getFieldErrors().forEach(fieldError ->
                        validationErrors.add(ValidationError.from(fieldError)));
                beanResult.getGlobalErrors().forEach(globalError ->
                        validationErrors.add(ValidationError.from(globalError)));
            });
            return this;
        }

        public Builder extractValidationErrorsFrom(MethodArgumentNotValidException e) {
            BindingResult bindingResult = e.getBindingResult();
            this.validationErrors = new ArrayList<>();
            bindingResult.getFieldErrors().forEach(fieldError ->
                    validationErrors.add(ValidationError.from(fieldError))
            );
            bindingResult.getGlobalErrors().forEach(globalError ->
                    validationErrors.add(ValidationError.from(globalError))
            );
            return this;
        }

        public Builder extractValidationErrorsFrom(ConstraintViolationException e) {
            this.validationErrors = new ArrayList<>();
            e.getConstraintViolations().forEach(violation -> {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                validationErrors.add(new ValidationError(field, message));
            });
            return this;
        }

        public ErrorMessageResponse build() {
            if (!showStackTrace) {
                stackTrace = null;
            }
            return new ErrorMessageResponse(
                message,
                status,
                error,
                path,
                stackTrace,
                timestamp,
                validationErrors
            );
        }
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), message);
        problemDetail.setTitle(error);
        problemDetail.setProperty("timestamp", timestamp.toString());

        if (stackTrace != null && !stackTrace.isEmpty()) {
            problemDetail.setProperty("stackTrace", stackTrace);
        }
        if (validationErrors != null && !validationErrors.isEmpty()) {
            problemDetail.setProperty("validationErrors", validationErrors);
        }
        return problemDetail;
    }
}