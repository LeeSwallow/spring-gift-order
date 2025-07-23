package gift.service.auth;

import gift.entity.UserRole;

import java.util.Set;

public interface AuthService {
    String login(String email, String password);
    String signup(String email, String password, Set<UserRole> roles);
}
