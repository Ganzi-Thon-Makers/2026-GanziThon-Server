package com.ganzithon.makers.platform.domain.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganzithon.makers.common.exception.GlobalExceptionHandler;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.security.AuthUserArgumentResolver;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.presentation.dto.response.MyPageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

  private UserService userService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    userService = mock(UserService.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthUserArgumentResolver())
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("마이페이지는 인증된 principal의 사용자 ID로 조회한다")
  void returnsMyPageForAuthenticatedPrincipal() throws Exception {
    authenticate();
    when(userService.getMyPage(7L))
        .thenReturn(new MyPageResponse(7L, "참가자", "participant", null, List.of()));

    mockMvc.perform(get("/api/v1/user/mypage"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value(7))
        .andExpect(jsonPath("$.data.name").value("참가자"));

    verify(userService).getMyPage(7L);
  }

  @Test
  @DisplayName("쿼리 사용자 ID로 인증 사용자의 마이페이지를 바꿀 수 없다")
  void ignoresUserIdQueryParameter() throws Exception {
    authenticate();
    when(userService.getMyPage(7L))
        .thenReturn(new MyPageResponse(7L, "참가자", "participant", null, List.of()));

    mockMvc.perform(get("/api/v1/user/mypage").param("userId", "999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value(7));

    verify(userService).getMyPage(7L);
  }

  @Test
  @DisplayName("미인증 마이페이지 요청은 사용자 ID를 보내도 401을 반환한다")
  void rejectsUnauthenticatedRequest() throws Exception {
    mockMvc.perform(get("/api/v1/user/mypage").param("userId", "7"))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("Swagger 인증 파라미터는 AuthPrincipal 타입이며 외부 입력에서 숨긴다")
  void hidesPrincipalInSwagger() throws Exception {
    java.lang.reflect.Parameter parameter = UserApi.class
        .getMethod("getMyPage", AuthPrincipal.class).getParameters()[0];

    assertThat(parameter.isAnnotationPresent(AuthUser.class)).isTrue();
    assertThat(parameter.getAnnotation(Parameter.class)).isNotNull();
    assertThat(parameter.getAnnotation(Parameter.class).hidden()).isTrue();
  }

  private void authenticate() {
    AuthPrincipal principal = new AuthPrincipal(7L, Role.PARTICIPANT, "sid-7");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
  }
}
