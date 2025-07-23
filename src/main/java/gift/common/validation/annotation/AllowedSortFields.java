package gift.common.validation.annotation;

import gift.common.validation.validator.AllowedSortFieldsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedSortFieldsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedSortFields {
    String[] value() default {};
    String message() default "허용되지 않은 정렬 필드입니다.";
    boolean showAllowedFields() default false;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
