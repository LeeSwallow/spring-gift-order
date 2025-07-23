package gift.common.validation.validator;

import gift.common.validation.annotation.KakaoNotContained;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class KakaoNotContainedValidator implements ConstraintValidator<KakaoNotContained, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return null == value || !value.contains("카카오");
    }
}
