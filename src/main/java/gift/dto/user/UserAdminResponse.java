package gift.dto.user;

import java.time.Instant;
import java.util.List;

public record UserAdminResponse(
    Long id,
    String email,
    String password,
    List<String> roles,
    Instant createdAt,
    Instant updatedAt
) {
}
