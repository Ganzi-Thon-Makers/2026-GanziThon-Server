package com.ganzithon.makers.platform.domain.auth.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.auth.application.AuthService;
import com.ganzithon.makers.platform.domain.auth.application.dto.request.LoginRequest;
import com.ganzithon.makers.platform.domain.auth.application.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/api/v1/auth")
@RequiredArgsConstructor
public class BackofficeAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(authService.adminLogin(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@AuthUser AuthPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.ok(SuccessResponse.of("로그아웃되었습니다."));
    }
}
