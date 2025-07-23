package gift.common.validation.validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCharSet implements ConstraintValidator<gift.common.validation.annotation.ValidCharSet, String> {
    // 한글, 영문, 숫자, 공백, 허용 특수문자만 가능, 최대 15자
    // \\p{L} : 모든 언어의 문자(한글 포함)
    // \\p{N} : 모든 숫자
    // 공백, (), [], +, -, &, /, _ 허용
    private static final String ALLOWED_PATTERN = "^[\\p{L}\\p{N} ()\\[\\]+\\-&/_]+$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.matches(ALLOWED_PATTERN);
    }
}
