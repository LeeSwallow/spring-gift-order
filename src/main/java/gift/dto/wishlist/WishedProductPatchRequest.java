package gift.dto.wishlist;

import jakarta.validation.constraints.NotNull;

public record WishedProductPatchRequest(
        @NotNull(message = "수정할 수량은 필수입니다.")
        Integer amount
) {
}
