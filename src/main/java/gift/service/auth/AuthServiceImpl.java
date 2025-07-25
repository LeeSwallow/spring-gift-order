package gift.service.auth;

import gift.common.exception.KakaoAuthorizationException;
import gift.common.exception.UnauthorizedException;
import gift.common.util.PasswordEncoder;
import gift.common.util.TokenProvider;
import gift.entity.User;
import gift.entity.type.UserRole;
import gift.external.KakaoTokenClient;
import gift.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final KakaoTokenClient kakaoOauth2Client;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserService userService,
            KakaoTokenClient kakaoOauth2Client,
            TokenProvider tokenProvider,
            PasswordEncoder passwordEncoder
        ) {
        this.userService = userService;
        this.kakaoOauth2Client = kakaoOauth2Client;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public String login(String email, String password) {
        User user;
        // sql 비용을 줄이기 위해 try-catch로 예외 처리
        try {
            user = userService.findByEmail(email);
        } catch (NoSuchElementException e) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return tokenProvider.generateToken(user.getId(), user.getUserRoles());
    }

    @Override
    public String kakaoLogin(String code, String error, String errorDescription) {
        if (code == null || code.isBlank()) {
            throw new KakaoAuthorizationException(error, errorDescription);
        }
        var tokenResponse = kakaoOauth2Client.getTokenResponse(code);
        return tokenResponse.accessToken();
    }

    @Override
    @Transactional
    public String signup(String email, String password, Set<UserRole> roles) {
        User user = new User(
            email,
            passwordEncoder.encode(password),
            roles
        );
        User savedUser = userService.create(user);
        return tokenProvider.generateToken(savedUser.getId(), savedUser.getUserRoles());
    }
}
