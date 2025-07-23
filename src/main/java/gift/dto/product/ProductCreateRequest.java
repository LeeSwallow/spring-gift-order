package gift.dto.product;

import gift.common.validation.annotation.KakaoNotContained;
import gift.common.validation.annotation.UniqueOptionName;
import gift.common.validation.annotation.ValidCharSet;
import gift.common.validation.group.AuthenticationGroups;
import gift.dto.option.OptionCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductCreateRequest(
    @NotNull(message = "상품명은 필수입니다.")
    @NotBlank(message = "상품명은 빈칸일 수 없습니다.")
    @KakaoNotContained(groups = {AuthenticationGroups.UserGroup.class})
    @ValidCharSet(message = "상품명은 문자, 숫자, 공백, 특수문자( ( ), [ ], +, -, &, /, _)만 허용됩니다.")
    @Size(max = 15, message = "상품명은 최대 15자까지 입력 가능합니다.")
    String name,
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    Long price,
    @NotNull(message = "이미지 URL은 필수입니다.")
    String imageUrl,

    @Valid
    @NotNull(message = "옵션 목록은 필수입니다.")
    @Size(min = 1, message = "상품은 최소 하나의 옵션을 가져야 합니다.")
    @UniqueOptionName
    List<OptionCreateRequest> options
) {
}
