package com.ganzithon.makers.platform.domain.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

class AuthControllerTest {

  private AuthService authService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new AuthController(authService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthUserArgumentResolver())
        .addFilters(new PrincipalInjectionFilter())
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

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
    @DisplayName("로그인 성공시 토큰과 role을 반환한다")
    void successLoginAndExpectToken() throws Exception {
      when(authService.login(any(LoginRequest.class)))
          .thenReturn(new LoginResponse("token", "refresh-token", Role.PARTICIPANT));

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"loginId\":\"yhy6391\",\"password\":\"yhy1145\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.accessToken").value("token"))
          .andExpect(jsonPath("$.data.role").value("PARTICIPANT"));
    }

    @Test
    @DisplayName("인증정보가 다르면 401 반환")
    void invalidCredentialsThrowException() throws Exception {
      when(authService.login(any(LoginRequest.class)))
          .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"loginId\":\"yhy6391\",\"password\":\"wrong\"}"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("loginId가 비어있으면 400을 반환한다")
    void whenEmptyLoginIDThrowException() throws Exception {
      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"loginId\":\"\",\"password\":\"yhy1145\"}"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class Logout {
    @Test
    @DisplayName("인증된 사용자는 로그아웃시 서비스에 principal이 전달된다")
    void whenAnAuthenticatedUserLogesOut () throws Exception {
      mockMvc.perform(post("/api/v1/auth/logout").header("X-Test-Auth", "1"))
          .andExpect(status().isOk());

      verify(authService).logout(any(AuthPrincipal.class));
    }

    @Test
    @DisplayName("미인증 사용자는 로그아웃시 401을 반환한다")
    void anAuauthenticatedUserReceivesThrowException() throws Exception {
      mockMvc.perform(post("/api/v1/auth/logout"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃중 세션이 이미 없어도 서비스 예외를 그대로 전달")
    void propagateTheServiceException () throws Exception {
      doThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
          .when(authService).logout(any(AuthPrincipal.class));

      mockMvc.perform(post("/api/v1/auth/logout").header("X-Test-Auth", "1"))
          .andExpect(status().isUnauthorized());
    }
  }
}
