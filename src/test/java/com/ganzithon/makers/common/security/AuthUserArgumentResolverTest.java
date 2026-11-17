package com.ganzithon.makers.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthUserArgumentResolverTest {

  private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setPrincipal(Object principal) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null));
  }

  @Test
  void AuthUser_어노테이션과_AuthPrincipal_타입이면_지원한다() throws Exception {
    MethodParameter param = new MethodParameter(
        AuthUserArgumentResolverTest.class.getDeclaredMethod("handler", AuthPrincipal.class), 0);

    assertThat(resolver.supportsParameter(param)).isTrue();
  }

  @Test
  void 어노테이션이_없으면_지원하지_않는다() throws Exception {
    MethodParameter param = new MethodParameter(
        AuthUserArgumentResolverTest.class.getDeclaredMethod("handler", AuthPrincipal.class), 0);
    param = new MethodParameter(param.getMethod(), 0) {
      @Override
      public <A extends java.lang.annotation.Annotation> boolean hasParameterAnnotation(Class<A> annotationType) {
        return false;
      }
    };

    assertThat(resolver.supportsParameter(param)).isFalse();
  }

  @Test
  void 인증된_principal을_반환한다() {
    AuthPrincipal principal = new AuthPrincipal(7L, Role.TEAM, "sid-1");
    setPrincipal(principal);

    Object result = resolver.resolveArgument(null, null, null, null);

    assertThat(result).isSameAs(principal);
  }

  @Test
  void 인증정보가_없으면_UNAUTHORIZED() {
    assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.UNAUTHORIZED);
  }

  @Test
  void principal이_AuthPrincipal이_아니면_UNAUTHORIZED() {
    setPrincipal("anonymous");

    assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.UNAUTHORIZED);
  }

  private void handler(@AuthUser AuthPrincipal principal) {
  }
}
