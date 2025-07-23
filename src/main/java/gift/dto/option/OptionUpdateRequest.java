package gift.dto.option;

import gift.common.validation.annotation.ValidCharSet;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record OptionUpdateRequest(
        @ValidCharSet(message = "옵션 이름은 문자, 숫자, 공백, 특수문자( ( ), [ ], +, -, &, /, _)만 허용됩니다.")
        @Size(max = 50, message = "옵션 이름은 최대 50자까지 입력 가능합니다.")
        String name,
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        @Max(value = 99_999_999, message = "수량은 1 억개 미만이여야 합니다.")
        Long quantity
) {
}
