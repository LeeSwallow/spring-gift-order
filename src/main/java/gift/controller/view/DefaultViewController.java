package gift.controller.view;

import gift.common.exception.AccessDeniedException;
import gift.common.model.CustomAuth;
import gift.common.util.TokenProvider;
import gift.dto.auth.LoginRequest;
import gift.entity.UserRole;
import gift.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class DefaultViewController {
    private static final String TOKEN_HEADER = "access-token";
    private final AuthService authService;
    private final Validator validator;
    private final TokenProvider tokenProvider;
    private final Integer expiration;

    public DefaultViewController(
            AuthService authService,
            Validator validator,
            TokenProvider tokenProvider,
            @Value("${gift.jwt.expiration}") Integer expiration
    ) {
        this.authService = authService;
        this.validator = validator;
        this.tokenProvider = tokenProvider;
        this.expiration = expiration;
    }

    private void validateLoginRequest(LoginRequest loginRequest) {
        Optional<ConstraintViolation<LoginRequest>> validationError = validator.validate(loginRequest)
                .stream()
                .findFirst();

        if (validationError.isPresent()) {
            throw new IllegalArgumentException(validationError.get().getMessage());
        }
    }

    private void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        if (tokenProvider.getAuthentication(token).role() != UserRole.ROLE_ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }

    private void saveTokenToCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_HEADER, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expiration);

        response.addCookie(cookie);
    }

    @GetMapping
    public String adminHome(
            @RequestAttribute("auth") CustomAuth auth,
            Model model
    ) {
        boolean isLogin = auth != null && auth.role() == UserRole.ROLE_ADMIN;
        model.addAttribute("title", "관리자 대시보드");
        model.addAttribute("isLogin", isLogin);
        return "admin/index";
    }

    @GetMapping("/login")
    public String adminLogin(
            @RequestAttribute("auth") CustomAuth auth,
            Model model
    ) {
        if (auth.role() == UserRole.ROLE_ADMIN) {
            return "redirect:/admin";
        }
        model.addAttribute("title", "관리자 로그인");
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(
            @ModelAttribute LoginRequest loginRequest,
             HttpServletResponse response,
             Model model
    ) {
        try {
            validateLoginRequest(loginRequest);
            String token = authService.login(loginRequest.email(), loginRequest.password());
            validateToken(token);

            saveTokenToCookie(token, response);
            return "redirect:/admin";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/login";
        }
    }
}