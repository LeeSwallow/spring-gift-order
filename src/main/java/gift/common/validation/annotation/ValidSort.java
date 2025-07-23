package gift.common.validation.annotation;

import gift.common.validation.validator.ValidSortValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidSortValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSort {
    String[] value() default {};
    String message() default "정렬 형식은 '필드:방향'이어야 합니다. 예: 'name[:asc]', 'price:desc'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
