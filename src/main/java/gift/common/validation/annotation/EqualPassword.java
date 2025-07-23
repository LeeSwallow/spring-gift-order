package gift.common.validation.annotation;

import gift.common.validation.validator.EqualPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {EqualPasswordValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface EqualPassword {
    String message() default "비밀번호가 일치하지 않습니다.";
    String field();
    String confirmField();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
