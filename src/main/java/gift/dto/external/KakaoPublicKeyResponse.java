package gift.dto.external;

import java.util.List;

public record KakaoPublicKeyResponse(
        List<KakaoPublicKey> keys
) {
}

