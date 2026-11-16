package com.ganzithon.makers.common.auth;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

// TODO: 인증(로그인) 기능 구현 완료 후 SecurityContext/JWT 기반으로 교체
@Component
public class CurrentUserProvider {

  private static final String USER_ID_HEADER = "X-User-Id";

  public Long getCurrentUserId(HttpServletRequest request) {
    String header = request.getHeader(USER_ID_HEADER);
    if (header == null || header.isBlank()) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    try {
      return Long.parseLong(header);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
  }

  public Long findCurrentUserId(HttpServletRequest request) {
    String header = request.getHeader(USER_ID_HEADER);
    if (header == null || header.isBlank()) {
      return null;
    }

    try {
      return Long.parseLong(header);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
