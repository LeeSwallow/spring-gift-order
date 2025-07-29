package gift.common.mapper;

import gift.dto.external.KakaoPublicKey;
import gift.entity.type.Provider;
import gift.external.KakaoTokenClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProviderMapper {
    private final Map<String, Provider>  issuerToProviderMap;
    private final Map<Provider, String> providerToIssuerMap;
    private final Map<Provider, Map<String, PublicKey>> providerToPublicKeyMap;

    private final KakaoTokenClient kakaoTokenClient;

    public ProviderMapper(
            @Value("${gift.jwt.issuer") String localIssuer,
            @Value("${gift.oauth.provider.kakao.baseurl}") String kakaoIssuer,
            KakaoTokenClient kakaoTokenClient
    ) {
        this.issuerToProviderMap = Map.of(localIssuer, Provider.EMAIL, kakaoIssuer, Provider.KAKAO);
        this.providerToIssuerMap = Map.of(Provider.EMAIL, localIssuer, Provider.KAKAO, kakaoIssuer);
        this.kakaoTokenClient = kakaoTokenClient;
        this.providerToPublicKeyMap = new HashMap<>();
        refreshKakaoKeys();
    }

    private void refreshKakaoKeys() {
        var response = kakaoTokenClient.getPublicKeyResponse();
        providerToPublicKeyMap.put(
                Provider.KAKAO,
                response.keys().stream()
                        .collect(Collectors.toMap(
                                KakaoPublicKey::kid,
                                KakaoPublicKey::toPublicKey
                        ))
        );
    }

    public void refreshPublicKeys(Provider provider) {
        switch (provider) {
            case KAKAO -> refreshKakaoKeys();
            // 추후 다른 프로바이더가 추가될 경우 여기에 추가
        }
    }

    public Provider toProvider(String  issuer) {
        return issuerToProviderMap.getOrDefault(issuer, Provider.UNKNOWN);
    }

    public String toIssuer(Provider provider) {
        return providerToIssuerMap.getOrDefault(provider, null);
    }

    public Map<String, PublicKey> getPublicKeys(Provider provider) {
        return providerToPublicKeyMap.getOrDefault(provider, null);
    }
}
