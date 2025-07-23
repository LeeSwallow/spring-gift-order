package gift.dto.user;

import gift.common.validation.annotation.ValidRoleList;
import gift.common.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserCreateRequest(
        @NotNull(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        @NotNull(message = "비밀번호는 필수입니다.")
        @ValidPassword
        String password,
        @ValidRoleList
        List<String> roles
) {
        public UserCreateRequest {
                if (roles == null || roles.isEmpty()) {
                        roles = List.of("ROLE_USER");
                }
        }
}
