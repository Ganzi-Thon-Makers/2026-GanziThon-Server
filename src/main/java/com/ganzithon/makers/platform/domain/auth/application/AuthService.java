package com.ganzithon.makers.platform.domain.auth.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.jwt.JwtProvider;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.LoginRequest;
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
    public void logout(AuthPrincipal principal) {
        loginSessionRepository.deleteBySessionId(principal.sessionId());
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
        loginSessionRepository.save(new LoginSession(
                user.getId(),
                sessionId,
                LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getExpiryMs()))));

        String accessToken = jwtProvider.createToken(user.getId(), user.getRole(), sessionId);
        return new LoginResponse(accessToken, user.getRole());
    }
}
