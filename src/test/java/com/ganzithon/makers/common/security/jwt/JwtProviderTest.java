package com.ganzithon.makers.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final long EXPIRY_MS = 86_400_000L; // 1 day
  private static final long REFRESH_EXPIRY_MS = 2_592_000_000L; // 30 days
  private static String secret;

  private JwtProvider jwtProvider;

  @BeforeEach
  void setUp() {
    if (secret == null) {
      byte[] bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
      secret = java.util.Base64.getEncoder().encodeToString(bytes);
    }
    jwtProvider = new JwtProvider(secret, EXPIRY_MS, REFRESH_EXPIRY_MS);
  }

  @Test
  void 생성한_토큰에서_userId_role_sessionId를_복원한다() {
    String token = jwtProvider.createToken(42L, Role.ADMIN, "sid-abc");

    Optional<AuthPrincipal> parsed = jwtProvider.parseAuthUser(token);

    assertThat(parsed).isPresent();
    assertThat(parsed.get().userId()).isEqualTo(42L);
    assertThat(parsed.get().role()).isEqualTo(Role.ADMIN);
    assertThat(parsed.get().sessionId()).isEqualTo("sid-abc");
  }

  @Test
  void 위조된_토큰은_empty를_반환한다() {
    SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
        java.util.Base64.getEncoder().encodeToString(new byte[32])));
    String forged = Jwts.builder()
        .subject("42")
        .claim("role", Role.SUPER_ADMIN.name())
        .claim("sid", "sid-abc")
        .signWith(otherKey)
        .compact();

    assertThat(jwtProvider.parseAuthUser(forged)).isEmpty();
  }

  @Test
  void 만료된_토큰은_empty를_반환한다() {
    JwtProvider expired = new JwtProvider(secret, -1000L, REFRESH_EXPIRY_MS);
    String token = expired.createToken(1L, Role.PARTICIPANT, "sid");

    assertThat(jwtProvider.parseAuthUser(token)).isEmpty();
  }

  @Test
  void 만료된_토큰은_isExpiredToken이_true다() {
    JwtProvider expired = new JwtProvider(secret, -1000L, REFRESH_EXPIRY_MS);
    String token = expired.createToken(1L, Role.PARTICIPANT, "sid");

    assertThat(jwtProvider.isExpiredToken(token)).isTrue();
  }

  @Test
  void 유효한_토큰과_위조된_토큰은_isExpiredToken이_false다() {
    String valid = jwtProvider.createToken(1L, Role.PARTICIPANT, "sid");

    assertThat(jwtProvider.isExpiredToken(valid)).isFalse();
    assertThat(jwtProvider.isExpiredToken("not-a-jwt")).isFalse();
  }

  @Test
  void sessionId가_없는_토큰은_empty를_반환한다() {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    String noSid = Jwts.builder()
        .subject("1")
        .claim("role", Role.PARTICIPANT.name())
        .signWith(key)
        .compact();

    assertThat(jwtProvider.parseAuthUser(noSid)).isEmpty();
  }

  @Test
  void 잘못된_형식의_토큰은_empty를_반환한다() {
    assertThat(jwtProvider.parseAuthUser("not-a-jwt")).isEmpty();
  }
}
