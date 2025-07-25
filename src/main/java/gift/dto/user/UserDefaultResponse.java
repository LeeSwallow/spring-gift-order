package gift.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import gift.entity.type.Provider;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDefaultResponse(
    Long id,
    String email,
    Provider provider
) {
}