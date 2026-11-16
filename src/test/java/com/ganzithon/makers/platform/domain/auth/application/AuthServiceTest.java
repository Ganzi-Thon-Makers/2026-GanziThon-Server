package com.ganzithon.makers.platform.domain.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.RefreshTokenHasher;
import com.ganzithon.makers.common.security.jwt.JwtProvider;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.LoginRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.RefreshRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.response.LoginResponse;
import com.ganzithon.makers.platform.domain.auth.domain.LoginSession;
import com.ganzithon.makers.platform.domain.auth.infrastructure.LoginSessionRepository;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  private static final long EXPIRY_MS = Duration.ofDays(1).toMillis();

  private UserRepository userRepository;
  private LoginSessionRepository loginSessionRepository;
  private JwtProvider jwtProvider;
  private PasswordEncoder passwordEncoder;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    loginSessionRepository = mock(LoginSessionRepository.class);
    jwtProvider = mock(JwtProvider.class);
    passwordEncoder = mock(PasswordEncoder.class);
    authService = new AuthService(userRepository, loginSessionRepository, jwtProvider, passwordEncoder);
    when(jwtProvider.getExpiryMs()).thenReturn(EXPIRY_MS);
  }

  private User user(Role role) {
    return User.create("yhy6391", "encoded", "name", "01012345678", "school", role, 1);
  }

  private void stubValidCredentials(User user) {
    when(userRepository.findByLoginId(user.getLoginId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("yhy1145", user.getPassword())).thenReturn(true);
    when(jwtProvider.createToken(any(), eq(user.getRole()), anyString())).thenReturn("token");
  }

  private LoginRequest request(User user) {
    return new LoginRequest(user.getLoginId(), "yhy1145");
  }

  @Nested
  class Login {

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("모든 role은 사용자 로그인이 가능하다")
    void allRolesCanLoginToPlatform(Role role) {
      User user = user(role);
      stubValidCredentials(user);

      LoginResponse response = authService.login(request(user));

      assertThat(response.accessToken()).isEqualTo("token");
      assertThat(response.role()).isEqualTo(role);
    }

    @Test
    @DisplayName("로그인 성공시 세션을 저장하고 JWT에 같은 sessionId를 넣는다")
    void savesSessionAndPutsSameSessionIdInJwt() {
      User user = user(Role.PARTICIPANT);
      stubValidCredentials(user);

      authService.login(request(user));

      ArgumentCaptor<LoginSession> sessionCaptor = ArgumentCaptor.forClass(LoginSession.class);
      verify(loginSessionRepository).save(sessionCaptor.capture());
      String sessionId = sessionCaptor.getValue().getSessionId();
      verify(jwtProvider).createToken(user.getId(), user.getRole(), sessionId);
    }

    @Test
    @DisplayName("세션 만료시간은 JWT 만료시간과 같다")
    void sessionExpiryMatchesJwtExpiry() {
      User user = user(Role.PARTICIPANT);
      stubValidCredentials(user);
      LocalDateTime before = LocalDateTime.now();

      authService.login(request(user));

      ArgumentCaptor<LoginSession> captor = ArgumentCaptor.forClass(LoginSession.class);
      verify(loginSessionRepository).save(captor.capture());
      LocalDateTime expiresAt = captor.getValue().getExpiresAt();
      assertThat(expiresAt).isBetween(
          before.plus(Duration.ofMillis(EXPIRY_MS)),
          LocalDateTime.now().plus(Duration.ofMillis(EXPIRY_MS)));
    }

    @Test
    @DisplayName("존재하지 않는 loginId면 INVALID_CREDENTIALS")
    void unknownLoginIdThrowsInvalidCredentials() {
      when(userRepository.findByLoginId("unknown")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "pw")))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_CREDENTIALS")
    void wrongPasswordThrowsInvalidCredentials() {
      User user = user(Role.PARTICIPANT);
      when(userRepository.findByLoginId(user.getLoginId())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      assertThatThrownBy(() -> authService.login(request(user)))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("TEAM 계정은 재로그인시 기존 세션을 삭제한다")
    void teamReloginDeletesExistingSessions() {
      User team = user(Role.TEAM);
      stubValidCredentials(team);

      authService.login(request(team));

      verify(loginSessionRepository).deleteByUserId(team.getId());
    }

    @ParameterizedTest
    @EnumSource(names = {"PARTICIPANT", "ADMIN", "SUPER_ADMIN"})
    @DisplayName("단일기기 정책이 아닌 role은 기존 세션을 유지한다")
    void nonSingleDeviceRolesKeepExistingSessions(Role role) {
      User user = user(role);
      stubValidCredentials(user);

      authService.login(request(user));

      verify(loginSessionRepository, never()).deleteByUserId(anyLong());
    }
  }

  @Nested
  class AdminLogin {

    @ParameterizedTest
    @EnumSource(names = {"ADMIN", "SUPER_ADMIN"})
    @DisplayName("백오피스 role은 어드민 로그인이 가능하다")
    void backofficeRolesCanAdminLogin(Role role) {
      User user = user(role);
      stubValidCredentials(user);

      LoginResponse response = authService.adminLogin(request(user));

      assertThat(response.accessToken()).isEqualTo("token");
      assertThat(response.role()).isEqualTo(role);
    }

    @ParameterizedTest
    @EnumSource(names = {"PARTICIPANT", "TEAM"})
    @DisplayName("백오피스 role이 아니면 FORBIDDEN이고 세션도 생성하지 않는다")
    void nonBackofficeRoleThrowsForbiddenAndCreatesNoSession(Role role) {
      User user = user(role);
      stubValidCredentials(user);

      assertThatThrownBy(() -> authService.adminLogin(request(user)))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN);
      verify(loginSessionRepository, never()).save(any());
    }
  }

  @Nested
  class Refresh {

    private static final String OLD_HASH = RefreshTokenHasher.hash("refresh-old");

    private LoginSession activeSession() {
      return new LoginSession(
          1L, "sid-1",
          LocalDateTime.now().minusDays(1),          // access는 만료된 상태여도 됨
          OLD_HASH,
          LocalDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("유효한 refreshToken이면 새 accessToken과 회전된 refreshToken을 반환한다")
    void validRefreshRotatesTokens() {
      LoginSession session = activeSession();
      User user = user(Role.PARTICIPANT);
      when(jwtProvider.getRefreshExpiryMs()).thenReturn(2_592_000_000L);
      when(loginSessionRepository.findByRefreshTokenHash(OLD_HASH)).thenReturn(Optional.of(session));
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(jwtProvider.createToken(any(), eq(Role.PARTICIPANT), eq("sid-1"))).thenReturn("new-access");

      LoginResponse response = authService.refresh(new RefreshRequest("refresh-old"));

      assertThat(response.accessToken()).isEqualTo("new-access");
      assertThat(response.refreshToken()).isNotEqualTo("refresh-old");
      assertThat(session.getRefreshTokenHash())
          .isEqualTo(RefreshTokenHasher.hash(response.refreshToken()))
          .isNotEqualTo(OLD_HASH);
      assertThat(session.getRefreshExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("같은 refreshToken은 두 번 사용할 수 없다")
    void rotatedRefreshTokenCannotBeReused() {
      LoginSession session = activeSession();
      when(loginSessionRepository.findByRefreshTokenHash(OLD_HASH)).thenReturn(Optional.of(session));
      when(userRepository.findById(1L)).thenReturn(Optional.of(user(Role.PARTICIPANT)));
      when(jwtProvider.getRefreshExpiryMs()).thenReturn(2_592_000_000L);

      authService.refresh(new RefreshRequest("refresh-old"));

      // 회전 후에는 예전 토큰으로 조회되지 않는다
      when(loginSessionRepository.findByRefreshTokenHash(OLD_HASH)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> authService.refresh(new RefreshRequest("refresh-old")))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("존재하지 않는 refreshToken이면 INVALID_REFRESH_TOKEN")
    void unknownRefreshTokenThrowsInvalidRefreshToken() {
      when(loginSessionRepository.findByRefreshTokenHash(RefreshTokenHasher.hash("nope")))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh(new RefreshRequest("nope")))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("만료된 refreshToken이면 세션을 삭제하고 INVALID_REFRESH_TOKEN을 던진다 (자동 로그아웃)")
    void expiredRefreshDeletesSessionAndThrows() {
      LoginSession expired = new LoginSession(
          1L, "sid-1",
          LocalDateTime.now().minusDays(1),
          OLD_HASH,
          LocalDateTime.now().minusSeconds(1));
      when(loginSessionRepository.findByRefreshTokenHash(OLD_HASH)).thenReturn(Optional.of(expired));

      assertThatThrownBy(() -> authService.refresh(new RefreshRequest("refresh-old")))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
      verify(loginSessionRepository).deleteBySessionId("sid-1");
    }
  }

  @Nested
  class Logout {

    @Test
    @DisplayName("로그아웃하면 현재 sessionId의 세션만 삭제한다")
    void logoutDeletesOnlyCurrentSession() {
      AuthPrincipal principal = new AuthPrincipal(1L, Role.PARTICIPANT, "sid-123");

      authService.logout(principal);

      verify(loginSessionRepository).deleteBySessionId("sid-123");
      verify(loginSessionRepository, never()).deleteByUserId(anyLong());
    }
  }
}
