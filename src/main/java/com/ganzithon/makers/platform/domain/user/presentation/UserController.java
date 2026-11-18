package com.ganzithon.makers.platform.domain.user.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.security.AuthPrincipal;
import com.ganzithon.makers.common.security.AuthUser;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.presentation.dto.response.MyPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/mypage")
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
            @AuthUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(SuccessResponse.of(userService.getMyPage(principal.userId())));
    }

}
