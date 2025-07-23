package gift.controller;

import gift.common.model.error.ErrorMessageResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping(value =  "/error", produces = "text/html")
    public String handleViewError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null && message != null) {
            model.addAttribute("statusCode",status);
            model.addAttribute("errorMessage", message.toString());
        }
        else {
            model.addAttribute("statusCode", 500);
            model.addAttribute("errorMessage", "알 수 없는 오류가 발생했습니다.");
        }

        log.error("html 요청 에서 오류 발생: status={}, message={}",status, message);

        return "error";
    }

    @RequestMapping(value = "/error", produces = "application/json")
    public ResponseEntity<ProblemDetail> handleApiError(HttpServletRequest request) {
        Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (exception == null) {
            exception = new Exception("일치하는 응답을 찾을 수 없습니다.");
        }
        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        HttpStatus status = HttpStatus.resolve((Integer) statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        log.error("API 요청 처리 중 오류 발생: status={}, exception={}", status, exception.getMessage(), exception);

        var errorMessage = new ErrorMessageResponse.Builder(request, exception, status)
                .build();
        return new ResponseEntity<>(errorMessage.toProblemDetail(), status);
    }
}
