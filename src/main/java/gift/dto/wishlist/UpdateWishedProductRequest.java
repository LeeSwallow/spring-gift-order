package gift.dto.wishlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateWishedProductRequest(
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 0, message = "수정할 수량은 0 이상이어야 합니다.")
    Integer quantity
) {
}
