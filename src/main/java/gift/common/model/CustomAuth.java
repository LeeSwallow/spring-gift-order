package gift.common.model;

import gift.entity.type.Provider;
import gift.entity.type.UserRole;

public record CustomAuth(
    Long userId,
    UserRole role,
    Provider provider
) {
}

