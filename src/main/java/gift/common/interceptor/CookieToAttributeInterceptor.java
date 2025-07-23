package gift.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class CookieToAttributeInterceptor implements HandlerInterceptor {
    private static final String COOKIE_TOKEN_HEADER = "access-token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (request.getCookies() == null) {
            return true; // 쿠키가 없으면 그냥 통과
        }

        Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_TOKEN_HEADER.equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    String token = cookie.getValue();
                    if (token != null && !token.isEmpty()) {
                        String authHeader = BEARER_PREFIX + token;
                        request.setAttribute(AUTHORIZATION_HEADER, authHeader);
                    }
                });
        return true;
    }
}
