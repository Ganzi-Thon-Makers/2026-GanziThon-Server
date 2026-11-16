package com.ganzithon.makers.common.security.jwt;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.common.security.SecurityErrorResponseWriter;
import com.ganzithon.makers.platform.domain.auth.infrastructure.LoginSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of("/api/v1/login", "/auth/backoffice/login");

    private final JwtProvider jwtProvider;
    private final LoginSessionRepository loginSessionRepository;
    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SKIP_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request);

        if (token != null) {
            Optional<AuthUser> parsed = jwtProvider.parseAuthUser(token);
            if (parsed.isPresent()) {
                AuthUser authUser = parsed.get();
                try {
                    validateSession(authUser);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(authUser, null, authUser.authorities()));
                } catch (BusinessException e) {
                    SecurityContextHolder.clearContext();
                    errorResponseWriter.write(response, e.getErrorCode(), e.getMessage());
                    return;
                }
            } else {
                SecurityContextHolder.clearContext();   // 익명으로 통과 → 보호 API는 401
            }
        }

        chain.doFilter(request, response);
    }

    private void validateSession(AuthUser authUser) {
        if (!loginSessionRepository.existsBySessionId(authUser.sessionId())) {
            throw new BusinessException(authUser.role().isSingleDevice()
                    ? ErrorCode.LOGGED_IN_ELSEWHERE
                    : ErrorCode.UNAUTHORIZED);
        }
    }
}
