package gift.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CookieToAttributeInterceptor implements HandlerInterceptor {
    private static final String COOKIE_TOKEN_HEADER = "access-token";
    private static final String COOKIE_CONN_TOKEN_HEADER = "X-Access-Token";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONN_TOKEN_HEADER = "X-Access-Token";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // header에 X-Access-Token이 있는 경우, 이를 attribute로 설정
        request.setAttribute(CONN_TOKEN_HEADER, request.getHeader(CONN_TOKEN_HEADER));

        if (request.getCookies() == null) {
            return true; // 쿠키가 없으면 그냥 통과
        }

        for (Cookie cookie : request.getCookies()) {
            // access-token을 찾아서 Authorization 헤더로 설정
            if (COOKIE_TOKEN_HEADER.equals(cookie.getName())) {
                String token = cookie.getValue();
                if (token != null && !token.isEmpty()) {
                    String authHeader = BEARER_PREFIX + token;
                    request.setAttribute(AUTHORIZATION_HEADER, authHeader);
                }

            // 연결된 서비스의 X-Access-Token을 찾아서 attribute로 설정
            } else if (COOKIE_CONN_TOKEN_HEADER.equals(cookie.getName())) {
                String connToken = cookie.getValue();
                if (connToken != null && !connToken.isEmpty()) {
                    request.setAttribute(CONN_TOKEN_HEADER, connToken);
                }
            }
        }
        return true;
    }
}
