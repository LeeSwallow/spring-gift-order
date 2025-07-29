package gift.config;

import gift.common.interceptor.CookieToAttributeInterceptor;
import gift.common.interceptor.JwtAuthenticateInterceptor;
import gift.common.resolver.AuthorizationArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CookieToAttributeInterceptor cookieToAttributeInterceptor;
    private final JwtAuthenticateInterceptor jwtAuthenticateInterceptor;
    private final AuthorizationArgumentResolver authorizationArgumentResolver;

    public WebConfig(
            CookieToAttributeInterceptor cookieToAttributeInterceptor,
            JwtAuthenticateInterceptor jwtAuthenticateInterceptor,
            AuthorizationArgumentResolver authorizationArgumentResolver
    ) {
        this.cookieToAttributeInterceptor = cookieToAttributeInterceptor;
        this.jwtAuthenticateInterceptor = jwtAuthenticateInterceptor;
        this.authorizationArgumentResolver = authorizationArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieToAttributeInterceptor)
                .addPathPatterns("/**"); // 관리자 경로에 대해 쿠키를 헤더로 변환하는 인터셉터 적용

        registry.addInterceptor(jwtAuthenticateInterceptor)
                .addPathPatterns("/**") // 모든 경로에 대해 인터셉터 적용
                .excludePathPatterns("/api/auth/**", "/error"); // 인증 관련 경로는 제외
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authorizationArgumentResolver); // AuthorizationArgumentResolver를 추가
    }
}
