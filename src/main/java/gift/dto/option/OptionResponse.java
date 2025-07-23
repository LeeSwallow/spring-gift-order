package gift.dto.option;

import java.time.Instant;

public record OptionResponse(
        Long id,
        String name,
        Long quantity,
        Instant createdAt,
        Instant updatedAt
) {
}
