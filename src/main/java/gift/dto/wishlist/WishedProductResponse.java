package gift.dto.wishlist;

import java.time.Instant;

public record WishedProductResponse(
        Long id,
        Long productId,
        String name,
        Long price,
        String imageUrl,
        Integer quantity,
        Long subtotal,
        Instant createdAt,
        Instant updatedAt
) {
}
