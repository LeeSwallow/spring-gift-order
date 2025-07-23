package gift.common.aop;

import gift.common.exception.AccessDeniedException;
import gift.common.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@ControllerAdvice(basePackages = "gift.controller.view")
public class GlobalViewExceptionHandler {
    private final static Logger log = LoggerFactory.getLogger(GlobalViewExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException e) {
        return "redirect:/admin/login";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleAuthenticationException(UnauthorizedException e) {
        return "redirect:/admin/login";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView handleNoSuchElementException(NoSuchElementException e) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
        modelAndView.addObject("errMessage", "요청한 리소스가 존재하지 않습니다: " + e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        modelAndView.addObject("errMessage", "서버 오류가 발생했습니다: " + e.getMessage());
        log.error("예상치 못한 예외가 발생했습니다.: {}",e.getMessage(), e);
        return modelAndView;
    }
}
