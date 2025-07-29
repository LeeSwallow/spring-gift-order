package gift.dto.order;

import java.time.Instant;

public record OrderResponse(
        Long id,
        Long optionId,
        Integer quantity,
        String message,
        Long totalPrice,
        Instant orderDateTime
) {

}
