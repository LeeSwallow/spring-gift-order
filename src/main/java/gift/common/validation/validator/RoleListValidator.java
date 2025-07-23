package gift.common.validation.validator;

import gift.common.validation.annotation.ValidRoleList;
import gift.entity.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class RoleListValidator implements ConstraintValidator<ValidRoleList, List<String>> {

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null 또는 빈 리스트는 유효하다고 간주
        }
        for (String role : value) {
            try {
                UserRole.valueOf(role);
            } catch(IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }
}
