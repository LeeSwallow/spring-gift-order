package gift.common.validation.validator;

import gift.common.validation.annotation.UniqueOptionName;
import gift.dto.option.OptionCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueOptionNameValidator implements ConstraintValidator<UniqueOptionName, List<OptionCreateRequest>> {
    @Override
    public boolean isValid(List<OptionCreateRequest> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null은 유효하다고 간주
        }

        Set<String> uniqueNames = new HashSet<>();
        for (OptionCreateRequest request : value) {
            if (uniqueNames.contains(request.name())) {
                return false; // 중복된 이름이 발견되면 유효하지 않음
            }
            uniqueNames.add(request.name());
        }
        return true;
    }
}
