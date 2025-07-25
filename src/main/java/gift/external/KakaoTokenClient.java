package gift.external;

import gift.common.exception.KakaoAuthorizationException;
import gift.dto.auth.KakaoErrorResponse;
import gift.dto.auth.KakaoTokenRequest;
import gift.dto.auth.KakaoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoTokenClient {
    private final String redirectUri;
    private final String restApiKey;
    private final RestClient restClient;

    public KakaoTokenClient(
            @Value("${gift.oauth.redirect-uri}") String baseRedirectUri,
            @Value("${gift.oauth.provider.kakao.baseurl}") String baseUrl,
            @Value("${gift.oauth.provider.kakao.rest-api-key}") String restApiKey
    ) {
        this.redirectUri = baseRedirectUri + "/kakao";
        this.restApiKey = restApiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    public KakaoTokenResponse getTokenResponse(String code) {
        var requestBody = new KakaoTokenRequest(
                "authorization_code",
                restApiKey,
                redirectUri,
                code
        );
        return restClient
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody.toMultiValueMap())
                .exchange((req, res)-> {
                    if (res.getStatusCode().is4xxClientError() || res.getStatusCode().is5xxServerError()) {
                        int statusCode = res.getStatusCode().value();
                        KakaoErrorResponse errorResponse = res.bodyTo(KakaoErrorResponse.class);
                        throw new KakaoAuthorizationException(
                                HttpStatus.valueOf(statusCode),
                                errorResponse.errorCode(),
                                errorResponse.errorDescription()
                        );
                    }
                    return res.bodyTo(KakaoTokenResponse.class);
                });
    }
}
