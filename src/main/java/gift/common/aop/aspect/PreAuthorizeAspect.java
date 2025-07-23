package gift.common.aop.aspect;

import gift.common.aop.annotation.PreAuthorize;
import gift.common.exception.AccessDeniedException;
import gift.common.exception.UnauthorizedException;
import gift.common.model.CustomAuth;
import gift.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Aspect
@Component
public class PreAuthorizeAspect {
    private static final String AUTHORIZATION_ATTRIBUTE = "auth";

    private final HttpServletRequest request;
    private final Validator validator;


    public PreAuthorizeAspect(HttpServletRequest request, Validator validator) {
        this.request = request;
        this.validator = validator;
    }
    private UserRole extractUserRoleFromAnnotation(MethodSignature signature){
        PreAuthorize preAuthorize = signature.getMethod().getAnnotation(PreAuthorize.class);
        if (preAuthorize == null || preAuthorize.value() == null) {
            throw new UnauthorizedException("PreAuthorize에 제대로된 UserRole이 설정되어 있지 않습니다:"
                    + signature.getMethod().getName());
        }
        return preAuthorize.value();
    }


    private List<Object> extractRequestBodies(Object[] args, MethodSignature signature) {
        List<Object> requestBodies = new ArrayList<>();
        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType().getSimpleName().equals("RequestBody")) {
                    requestBodies.add(args[i]);
                    break;
                }
            }
        }
        return requestBodies;
    }

    @Around("@annotation(gift.common.aop.annotation.PreAuthorize)")
    public Object preAuthorize(ProceedingJoinPoint joinPoint) throws Throwable {
        CustomAuth auth = (CustomAuth) request.getAttribute(AUTHORIZATION_ATTRIBUTE);
        if (auth == null) {
            throw new UnauthorizedException("인증 정보가 없습니다. 요청 헤더에 Authorization 헤더를 추가해야 합니다.");
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        UserRole targetRole = extractUserRoleFromAnnotation(signature);

        if (targetRole.getPriority() > auth.role().getPriority()) {
            throw new AccessDeniedException("접근 권한이 없습니다. 현재 사용자 역할: " + auth.role()
                    + ", 요청된 역할: " + targetRole);
        }
        Object[] args = joinPoint.getArgs();
        List<Object> requestBodies = extractRequestBodies(args, signature);

        // 유효성 검사할 DTO가 없으면 바로 proceed
        if (requestBodies.isEmpty()) {
            return joinPoint.proceed();
        }

        Class<?> authGroup = switch (auth.role()) {
            case ROLE_GUEST -> gift.common.validation.group.AuthenticationGroups.GuestGroup.class;
            case ROLE_ADMIN -> gift.common.validation.group.AuthenticationGroups.AdminGroup.class;
            case ROLE_MD -> gift.common.validation.group.AuthenticationGroups.MdGroup.class;
            case ROLE_USER -> gift.common.validation.group.AuthenticationGroups.UserGroup.class;
        };

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        for (Object requestBody : requestBodies) {
            if (requestBody != null) {
              violations.addAll(validator.validate(requestBody, authGroup));
            }
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("유효하지 않은 요청입니다.", violations);
        }
        return joinPoint.proceed();
    }
}
