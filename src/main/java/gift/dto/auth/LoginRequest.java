package gift.dto.auth;

import gift.common.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;

public record LoginRequest(
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,
    @ValidPassword(message = "비밀번호는 최소 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    String password
) {
}
