package gift.dto.external;

import gift.common.exception.CriticalServerException;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public record KakaoPublicKey(
        String kid,
        String kty,
        String use,
        String alg,
        String n,
        String e
) {
    public PublicKey toPublicKey() {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CriticalServerException("kakao public Key를 변환 할 수 없습니다. : " + e.getMessage(), e);
        }

    }
}
