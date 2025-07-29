package gift.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.common.mapper.ProviderMapper;
import gift.common.model.TokenInfo;
import gift.entity.type.Provider;
import gift.entity.type.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.*;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TokenProvider implements InitializingBean {
    private static final String AUTHORITIES_KEY = "auth";
    private static final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private final ProviderMapper providerMapper;
    private final String secret;
    private final Long expiration;
    private SecretKey secretKey;


    public TokenProvider(
        ProviderMapper providerMapper,
        @Value("${gift.jwt.secret}") @Valid String secret,
        @Value("${gift.jwt.expiration}") @Valid Long expiration
    ) {
        this.providerMapper = providerMapper;
        this.secret = secret;
        this.expiration = expiration;
    }

    @Override
    public void afterPropertiesSet() {
        if (secret == null || secret.isEmpty()) {
            throw new BeanInitializationException(
                "설정을 통해 올바른 JWT 비밀 키를 제공해야 합니다.: gift.jwt.secret=???"
            );
        }
        if (secret.length() < 32) {
            throw new BeanInitializationException(
                "설정을 통해 최소 32자 이상의 JWT 비밀 키를 제공해야 합니다.: gift.jwt.secret=???"
            );
        }
        if (expiration == null || expiration <= 0) {
            throw new BeanInitializationException(
                "설정을 통해 올바른 JWT 만료 기간을 제공해야 합니다.: gift.jwt.expiration=???"
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private PublicKey getPublicKeyFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        String bodyJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        try {
            String kid = mapper.convertValue(mapper.readTree(headerJson), Map.class).get("kid").toString();
            String iss = mapper.convertValue(mapper.readTree(bodyJson), Map.class).get("iss").toString();
            Provider provider = providerMapper.toProvider(iss);
            Map<String, PublicKey> publicKeys = providerMapper.getPublicKeys(provider);
            if (publicKeys == null || !publicKeys.containsKey(kid)) {
                log.warn("유효하지 않은 키 ID(kid): {}", kid);
                return null;
            }
            return publicKeys.get(kid);
        } catch (Exception e) {
            return null;
        }
    }

    private Claims getClaimsWithVerification(String token) {
       try {
           return Jwts.parser()
                   .verifyWith(this.secretKey)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();

       } catch(JwtException e) {
           log.debug("비밀 키로 JWT를 파싱하는 중 오류 발생, 공개키로 재시도: {}", e.getMessage());
              PublicKey publicKey = getPublicKeyFromToken(token);
              if (publicKey == null) {
                  log.debug("공개키 찾기를 실패했습니다.");
                  throw new JwtException("유효하지 않은 JWT 토큰 입니다.");
            }
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
       }
    }

    public String generateToken(Long userId, Set<UserRole> authorities, Provider provider) {
        return generateToken(userId, authorities, provider, this.expiration);
    }

    public String generateToken(Long userId, Set<UserRole> authorities, Provider provider, Long expiration) {
        Instant now = Instant.now(Clock.systemDefaultZone());
        if (expiration == null || expiration <= 0) {
            throw new IllegalArgumentException("만료 기간은 null이거나 0 이하일 수 없습니다.");
        }
        Instant expiryDate = now.plusSeconds(expiration);

        String authoritiesString = authorities.stream()
                .map(UserRole::toString)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userId.toString())
                .claim(AUTHORITIES_KEY, authoritiesString)
                .issuedAt(Date.from(now))
                .issuer(providerMapper.toIssuer(provider)) // 이 서버에서 발급한 토큰임을 나타냅니다.
                .expiration(Date.from(expiryDate))
                .signWith(this.secretKey)
                .compact();
    }

    public TokenInfo getTokenInfo(String token) {
        Claims claims = getClaimsWithVerification(token);

        // Extract  ID from claims
        String IdString = claims.getSubject();
        if (IdString == null || IdString.isEmpty()) {
            return null;
        }

        // Extract provider from claims
        String issuer = claims.getIssuer();
        Provider provider = providerMapper.toProvider(issuer);

        UserRole role = switch (provider) {
            case UNKNOWN ->  UserRole.ROLE_GUEST; // 알 수 없는 프로바이더는 게스트로 처리
            case EMAIL -> { // 이메일 인증을 통한 사용자
                var authoritiesString = claims.get(AUTHORITIES_KEY, String.class);
                yield Stream.of(authoritiesString.split(","))
                        .map(UserRole::fromString)
                        .max(Comparator.comparing(UserRole::getPriority))
                        .orElse(UserRole.ROLE_GUEST);
            }
            default -> UserRole.ROLE_USER; // 외부 인증을 통한 사용자
        };

        return new TokenInfo(token, IdString, role, provider);
    }

    public Boolean validateToken(String token) {
        try {
            getClaimsWithVerification(token);
            return true;
        } catch (SecurityException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (Exception e) {
            log.warn("유효하지 않은 JWT 토큰입니다.");
        }
        return false;
    }
}
