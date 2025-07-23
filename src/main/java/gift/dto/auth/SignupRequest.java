package gift.dto.auth;

import gift.common.validation.annotation.EqualPassword;
import gift.common.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@EqualPassword(
    field = "password",
    confirmField = "passwordConfirm"
)
public record SignupRequest(
    @NotNull(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,
    @NotNull(message = "비밀번호는 필수입니다.")
    @ValidPassword(message = "비밀번호는 최소 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    String password,
    @NotNull(message = "비밀번호 확인은 필수입니다.")
    String passwordConfirm
) {
}
