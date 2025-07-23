package gift.common.validation.validator;

import gift.common.validation.annotation.EqualPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class EqualPasswordValidator  implements ConstraintValidator<EqualPassword, Object> {
    private String passwordField;
    private String confirmField;

    @Override
    public void initialize(EqualPassword constraintAnnotation) {
        this.passwordField = constraintAnnotation.field();
        this.confirmField = constraintAnnotation.confirmField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field passwordField = value.getClass().getDeclaredField(this.passwordField);
            Field confirmField = value.getClass().getDeclaredField(this.confirmField);

            passwordField.setAccessible(true);
            confirmField.setAccessible(true);

            Object passwordValue = passwordField.get(value);
            Object confirmValue = confirmField.get(value);

            if (passwordValue == null || confirmValue == null) {
                return false;
            }
            return passwordValue.equals(confirmValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }
}
