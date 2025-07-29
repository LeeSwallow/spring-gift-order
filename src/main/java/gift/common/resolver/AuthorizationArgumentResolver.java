package gift.common.resolver;

import gift.common.model.CustomAuth;
import gift.common.model.TokenInfo;
import gift.entity.type.Provider;
import gift.entity.type.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthorizationArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String TOKEN_ATTRIBUTE = "tokenInfo";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CustomAuth.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        try {
            TokenInfo tokenInfo = (TokenInfo) webRequest.getAttribute(TOKEN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

            if (tokenInfo == null || tokenInfo.provider() == null || tokenInfo.provider() == Provider.UNKNOWN) {
                return new CustomAuth(null, UserRole.ROLE_GUEST, Provider.UNKNOWN);
            }
            Long userId = Long.parseLong(tokenInfo.id());
            return new CustomAuth(userId, tokenInfo.role(), tokenInfo.provider());

        } catch (ClassCastException e) {
            // 토큰 정보가 잘못된 형식인 경우, 예외 처리
            return new CustomAuth(null, UserRole.ROLE_GUEST, Provider.UNKNOWN);
        } catch (Exception e) {
            throw new RuntimeException("예기치 못한 오류가 발생했습니다.: " + e.getMessage(), e);
        }
    }
}
