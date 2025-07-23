package gift.common.util;

import gift.entity.UserRole;
import gift.common.model.CustomAuth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
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
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TokenProvider implements InitializingBean {
    private static final String AUTHORITIES_KEY = "auth";
    private static final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private final String secret;
    private final Long expiration;
    private SecretKey secretKey;


    public TokenProvider(
        @Value("${gift.jwt.secret}") @Valid String secret,
        @Value("${gift.jwt.expiration}") @Valid Long expiration
    ) {
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

    public String generateToken(Long userId, Set<UserRole> authorities) {
        Instant now = Instant.now(Clock.systemDefaultZone());
        Instant expiryDate = now.plusSeconds(expiration);

        String authoritiesString = authorities.stream()
                .map(UserRole::toString)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userId.toString())
                .claim(AUTHORITIES_KEY, authoritiesString)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(this.secretKey)
                .compact();
    }

    public CustomAuth getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Extract user ID from claims
        String userIdString = claims.getSubject();
        if (userIdString == null || userIdString.isEmpty()) {
            return null;
        }
        Long userId = Long.valueOf(userIdString);

        // Extract authorities from claims
        String authoritiesString = claims.get(AUTHORITIES_KEY, String.class);
        Set<UserRole> authorities = Stream.of(authoritiesString.split(","))
                .map(UserRole::fromString)
                .collect(Collectors.toSet());
        if (authorities.isEmpty()) {
            return null;
        }

        return new CustomAuth(userId, authorities);
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parse(token);
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
