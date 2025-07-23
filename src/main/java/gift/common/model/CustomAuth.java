package gift.common.model;

import gift.entity.UserRole;

import java.util.Comparator;
import java.util.Set;

public record CustomAuth(
    Long userId,
    UserRole role
) {
    public CustomAuth(Long userId, Set<UserRole> roles) {
        this(userId, roles.stream().max(Comparator.comparing(UserRole::getPriority))
                .orElseThrow(() -> new IllegalArgumentException("사용자 역할이 비어 있습니다!")));
    }
}

