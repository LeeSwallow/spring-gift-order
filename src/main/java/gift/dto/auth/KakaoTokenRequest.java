package gift.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.util.MultiValueMap;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoTokenRequest(
        String grantType,
        String clientId,
        String redirectUri,
        String code
) {
    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        map.add("client_id", clientId);
        map.add("redirect_uri", redirectUri);
        map.add("code", code);
        return map;
    }
}
