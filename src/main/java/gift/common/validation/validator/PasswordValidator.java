package gift.common.validation.validator;

import gift.common.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;

public class PasswordValidator  implements ConstraintValidator<ValidPassword, String> {
    // 비밀번호는 영문, 숫자, 특수문자 조합으로 8자 이상 20자 이하
    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$";

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.matches(PASSWORD_PATTERN);
    }

}
