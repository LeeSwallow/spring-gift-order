package gift.controller.api;

import gift.dto.auth.LoginRequest;
import gift.dto.auth.SignupRequest;
import gift.dto.auth.TokenResponse;
import gift.entity.UserRole;
import gift.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signUp(
            @Valid @RequestBody SignupRequest request
            ) {
        Set<UserRole> defaultRoles = Set.of(UserRole.ROLE_USER);
        String token = authService.signup(
                request.email(),
                request.password(),
                defaultRoles
        );
        return new ResponseEntity<>(new TokenResponse(token), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String token = authService.login(request.email(), request.password());
        return new ResponseEntity<>(new TokenResponse(token), HttpStatus.OK);
    }
}
