package com.ganzithon.makers.common.security.jwt;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.SecurityPaths;
import com.ganzithon.makers.platform.domain.auth.infrastructure.LoginSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of(SecurityPaths.LOGIN, SecurityPaths.BACKOFFICE_LOGIN);

    private final JwtProvider jwtProvider;
    private final LoginSessionRepository loginSessionRepository;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            LoginSessionRepository loginSessionRepository,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.jwtProvider = jwtProvider;
        this.loginSessionRepository = loginSessionRepository;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SKIP_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request);

        if (token != null) {
            Optional<AuthPrincipal> parsed = jwtProvider.parseAuthUser(token);
            if (parsed.isPresent()) {
                AuthPrincipal principal = parsed.get();
                try {
                    validateSession(principal);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
                } catch (BusinessException e) {
                    SecurityContextHolder.clearContext();
                    resolver.resolveException(request, response, null, e);   // 기존 예외 처리에 위임
                    return;
                }
            } else {
                SecurityContextHolder.clearContext();   // 익명으로 통과 → 보호 API는 401
            }
        }

        chain.doFilter(request, response);
    }

    private void validateSession(AuthPrincipal principal) {
        if (!loginSessionRepository.existsBySessionId(principal.sessionId())) {
            throw new BusinessException(principal.role().isSingleDevice()
                    ? ErrorCode.LOGGED_IN_ELSEWHERE
                    : ErrorCode.UNAUTHORIZED);
        }
    }
}
