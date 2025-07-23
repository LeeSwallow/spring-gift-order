package gift.common.validation.validator;

import gift.common.validation.annotation.ValidSort;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ValidSortValidator implements ConstraintValidator<ValidSort, List<String>> {
    private static final Pattern ALLOW_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+?$");
    private static final Set<String> SORT_DIRECTIONS = Set.of("asc","desc");
    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null 값은 유효함
        }

        if (!ALLOW_PATTERN.matcher(value.getFirst()).matches() || SORT_DIRECTIONS.contains(value.getFirst())) {
            return false; // 첫 번째 요소가 유효하지 않은 경우
        }

        for (int i = 1; i < value.size(); i++) {
            if (ALLOW_PATTERN.matcher(value.get(i)).matches()) {
                if (SORT_DIRECTIONS.contains(value.get(i)) && SORT_DIRECTIONS.contains(value.get(i - 1))) {
                    return false; // 연속된 정렬 방향은 허용하지 않음
                }
            } else {
                return false; // 유효하지 않은 필드 형식
            }
        }
        return true; // 모든 요소가 유효한 경우
    }
}
