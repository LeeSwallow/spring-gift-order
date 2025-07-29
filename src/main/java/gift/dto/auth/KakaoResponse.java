package gift.dto.auth;

public record KakaoResponse(
        String token,
        String connectToken
) {
}
