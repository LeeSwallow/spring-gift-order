package gift.common.validation.annotation;

import gift.common.validation.validator.KakaoNotContainedValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = KakaoNotContainedValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface KakaoNotContained {
    String message() default "\"카카오\"가 포함된 상품명은 MD 승인이 필요합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
