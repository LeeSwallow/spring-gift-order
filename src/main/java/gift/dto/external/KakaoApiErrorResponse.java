package gift.dto.external;

public record KakaoApiErrorResponse(
        int code,
        String msg
) {
}
