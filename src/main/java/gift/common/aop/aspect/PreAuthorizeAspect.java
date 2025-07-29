package gift.common.aop.aspect;

import gift.common.aop.annotation.PreAuthorize;
import gift.common.exception.AccessDeniedException;
import gift.common.exception.UnauthorizedException;
import gift.common.model.TokenInfo;
import gift.common.validation.group.AuthenticationGroups;
import gift.entity.type.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.util.*;

@Aspect
@Component
public class PreAuthorizeAspect {
    private static final String TOKEN_ATTRIBUTE = "tokenInfo";

    private final HttpServletRequest request;
    private final Validator validator;

    public PreAuthorizeAspect(HttpServletRequest request, Validator validator) {
        this.request = request;
        this.validator = validator;
    }

    private void checkRolePriority(UserRole role, MethodSignature signature) {
        TokenInfo tokenInfo = (TokenInfo) request.getAttribute(TOKEN_ATTRIBUTE);
        if (tokenInfo == null) {
            throw new UnauthorizedException("인증 정보가 없습니다. 요청 헤더에 Authorization 헤더를 추가해야 합니다.");
        }

        PreAuthorize preAuthorize = signature.getMethod().getAnnotation(PreAuthorize.class);
        if (preAuthorize == null || preAuthorize.value() == null) {
            throw new UnauthorizedException("PreAuthorize에 제대로된 UserRole이 설정되어 있지 않습니다:"
                    + signature.getMethod().getName());
        }
        UserRole targetRole = preAuthorize.value();

        if (targetRole.getPriority() > role.getPriority()) {
            throw new AccessDeniedException("접근 권한이 없습니다. 현재 사용자 역할: " + role + ", 요청된 역할: " + targetRole);
        }
    }

    private void validateArguments(UserRole role, Object[] args, Annotation[][] paramAnnotations) {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        Class<?> authGroup = switch (role) {
            case ROLE_GUEST -> AuthenticationGroups.GuestGroup.class;
            case ROLE_ADMIN -> AuthenticationGroups.AdminGroup.class;
            case ROLE_MD -> AuthenticationGroups.MdGroup.class;
            case ROLE_USER -> AuthenticationGroups.UserGroup.class;
        };

        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == RequestBody.class) {
                    Object requestBody = args[i];
                    if (requestBody != null) {
                        violations.addAll(validator.validate(requestBody, authGroup));
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("유효하지 않은 요청입니다.", violations);
        }
    }

    @Around("@annotation(gift.common.aop.annotation.PreAuthorize)")
    public Object preAuthorize(ProceedingJoinPoint joinPoint) throws Throwable {

        // request에 토큰 정보가 있는 지 확인
        TokenInfo tokenInfo = (TokenInfo) request.getAttribute(TOKEN_ATTRIBUTE);
        if (tokenInfo == null) {
            throw new UnauthorizedException("인증 정보가 없습니다. 요청 헤더에 Authorization 헤더를 추가해야 합니다.");
        }

        // 유효한 우선순위를 만족하는 지 확인
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        checkRolePriority(tokenInfo.role(), signature);

        // 요청 인자에 대한 유효성 검사
        Object[] args = joinPoint.getArgs();
        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();
        validateArguments(tokenInfo.role(), args, paramAnnotations);

        return joinPoint.proceed();
    }
}
