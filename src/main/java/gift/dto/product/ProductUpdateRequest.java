package gift.dto.product;

import gift.common.validation.annotation.KakaoNotContained;
import gift.common.validation.annotation.ValidCharSet;
import gift.common.validation.group.AuthenticationGroups;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record ProductUpdateRequest (
    @ValidCharSet
    @KakaoNotContained(groups = {AuthenticationGroups.UserGroup.class})
    @Size(max = 15, message = "상품명은 최대 15자까지 입력 가능합니다.")
    String name,
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    Long price,
    String imageUrl
) {
}
