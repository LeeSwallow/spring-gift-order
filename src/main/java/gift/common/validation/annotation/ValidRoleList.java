package gift.common.validation.annotation;

import gift.common.validation.validator.RoleListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoleListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoleList {
    String message() default "유효하지 않은 역할 목록입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
