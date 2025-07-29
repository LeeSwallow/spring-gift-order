package gift.common.model;

import gift.entity.type.Provider;
import gift.entity.type.UserRole;

public record TokenInfo(
        String value,
        String id,
        UserRole role,
        Provider provider
) {
}
