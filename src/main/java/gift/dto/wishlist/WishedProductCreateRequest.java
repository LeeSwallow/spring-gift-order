package gift.dto.wishlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WishedProductCreateRequest(
    @NotNull(message = "제품 ID는 필수입니다.")
    Long productId,

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    Integer quantity
) {
    public WishedProductCreateRequest {
        if (quantity == null) {
            quantity = 1; // 기본값 설정
        }
    }
}

