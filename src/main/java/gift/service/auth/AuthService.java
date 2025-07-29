package gift.service.auth;

import gift.dto.auth.KakaoResponse;
import gift.entity.type.UserRole;

import java.util.Set;

public interface AuthService {
    String login(String email, String password);
    KakaoResponse kakaoLogin(String code, String error, String errorDescription);
    String signup(String email, String password, Set<UserRole> roles);
}
