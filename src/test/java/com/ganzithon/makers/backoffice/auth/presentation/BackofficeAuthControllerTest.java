package com.ganzithon.makers.backoffice.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.exception.GlobalExceptionHandler;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUserArgumentResolver;
import com.ganzithon.makers.platform.domain.auth.application.AuthService;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.LoginRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.response.LoginResponse;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

class BackofficeAuthControllerTest {

  private AuthService authService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new BackofficeAuthController(authService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthUserArgumentResolver())
        .addFilters(new PrincipalInjectionFilter())
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // AuthControllerTest 의 필터와 동일 (패키지가 달라 접근할 수 없어 복사)
  static class PrincipalInjectionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws java.io.IOException, jakarta.servlet.ServletException {
      if (request.getHeader("X-Test-Auth") != null) {
        AuthPrincipal principal = new AuthPrincipal(1L, Role.PARTICIPANT, "sid-1");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
      }
      chain.doFilter(request, response);
    }
  }

  @Nested
  class Login {

    @Test
    @DisplayName("어드민 로그인 성공시 토큰과 role을 반환한다")
    void whenAdminLoginSuccess() throws Exception {
      when(authService.adminLogin(any(LoginRequest.class)))
          .thenReturn(new LoginResponse("token", "refresh-token", Role.SUPER_ADMIN));

      mockMvc.perform(post("/backoffice/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"loginId\":\"admin01\",\"password\":\"admin1234\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.accessToken").value("token"))
          .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("어드민 권한이 아닌경우 예외처리")
    void ifNotRoleAdminThrowException() throws Exception {
      when(authService.adminLogin(any(LoginRequest.class)))
          .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

      mockMvc.perform(post("/backoffice/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"loginId\":\"yhy6391\",\"password\":\"yhy1145\"}"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class Logout {

    @Test
    @DisplayName(" 인증된 사용자는 로그아웃시 서비스에 principal이 전달된다")
    void whenLogoutWithPrincipalUser() throws Exception {
      mockMvc.perform(post("/backoffice/api/v1/auth/logout").header("X-Test-Auth", "1"))
          .andExpect(status().isOk());

      verify(authService).logout(any(AuthPrincipal.class));
    }

    @Test
    @DisplayName("미 인증된 사용자는 로그아웃시 401 반환")
    void whenLogoutWithoutPrincipalUserThrowException() throws Exception {
      mockMvc.perform(post("/backoffice/api/v1/auth/logout"))
          .andExpect(status().isUnauthorized());
    }
  }
}
