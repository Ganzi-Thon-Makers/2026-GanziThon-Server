package com.ganzithon.makers.platform.domain.auth.application;

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
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        return startSession(user);
    }

    @Transactional
    public LoginResponse adminLogin(LoginRequest request) {
        User user = authenticate(request);
        if (!user.getRole().isBackoffice()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return startSession(user);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        LoginSession session = loginSessionRepository
                .findByRefreshTokenHash(RefreshTokenHasher.hash(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (session.isRefreshExpired(LocalDateTime.now())) {
            loginSessionRepository.deleteBySessionId(session.getSessionId());   // 자동 로그아웃
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        String newRefreshToken = UUID.randomUUID().toString();
        session.rotateRefreshToken(
                RefreshTokenHasher.hash(newRefreshToken),
                LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshExpiryMs())));

        String accessToken = jwtProvider.createToken(user.getId(), user.getRole(), session.getSessionId());
        return new LoginResponse(accessToken, newRefreshToken, user.getRole());
    }

    @Transactional
    public void logout(AuthPrincipal principal) {
        loginSessionRepository.deleteBySessionId(principal.sessionId());
    }

    // 해당 회원의 모든 로그인 세션 삭제 (강제 로그아웃)
    @Transactional
    public void logoutAll(Long userId) {
        loginSessionRepository.deleteByUserId(userId);
    }

    private User authenticate(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return user;
    }

    private LoginResponse startSession(User user) {
            if (user.getRole().isSingleDevice()) {
                loginSessionRepository.deleteByUserId(user.getId());
        }

        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        loginSessionRepository.save(new LoginSession(
                user.getId(),
                sessionId,
                now.plus(Duration.ofMillis(jwtProvider.getExpiryMs())),
                RefreshTokenHasher.hash(refreshToken),
                now.plus(Duration.ofMillis(jwtProvider.getRefreshExpiryMs()))));

        String accessToken = jwtProvider.createToken(user.getId(), user.getRole(), sessionId);
        return new LoginResponse(accessToken, refreshToken, user.getRole());
    }
}
