package com.ganzithon.makers.common.security.jwt;

import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SESSION_ID = "sid";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey key;
    @Getter
    private final long expiryMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-ms}") long expiryMs
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiryMs = expiryMs;
    }

    public String createToken(Long userId, Role role, String sessionId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(key)
                .compact();
    }

    public Optional<AuthUser> parseAuthUser(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            String sessionId = claims.get(CLAIM_SESSION_ID, String.class);
            if (sessionId == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthUser(
                    Long.valueOf(claims.getSubject()),
                    Role.valueOf(claims.get(CLAIM_ROLE, String.class)),
                    sessionId));
        } catch (JwtException | IllegalArgumentException e) {   // 위조, 만료, 잘못된 role/subject 등
            return Optional.empty();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
