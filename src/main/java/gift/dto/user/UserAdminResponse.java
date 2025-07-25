package gift.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import gift.entity.type.Provider;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserAdminResponse(
    Long id,
    String email,
    String password,
    String clientId,
    Provider provider,
    List<String> roles,
    Instant createdAt,
    Instant updatedAt
) {
}
