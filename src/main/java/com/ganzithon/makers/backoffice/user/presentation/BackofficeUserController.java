package com.ganzithon.makers.backoffice.user.presentation;

import com.ganzithon.makers.backoffice.user.application.BackofficeUserService;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserCreateRequest;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserDetailResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserUpdateRequest;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Backoffice - 사용자 관리", description = "참가자(PARTICIPANT)만 대상")
@RestController
@RequestMapping("/backoffice/api/v1/users")
@RequiredArgsConstructor
public class BackofficeUserController {

  private final BackofficeUserService backofficeUserService;

  @Operation(
      summary = "참가자 목록 조회/검색",
      description = "이름 가나다순. 조건을 함께 주면 모두 만족하는 참가자만 조회합니다. "
          + "페이지네이션 없이 전체를 반환합니다.")
  @GetMapping
  public ResponseEntity<SuccessResponse<List<UserListResponse>>> getUsers(
      @Parameter(description = "이름 검색어 (일부 일치)", example = "김")
      @RequestParam(required = false) String keyword,
      @Parameter(description = "럭키드로우 응모 여부 (생략 시 전체)")
      @RequestParam(required = false) Boolean luckyDrawEntered,
      @Parameter(description = "수료 여부 (생략 시 전체)")
      @RequestParam(required = false) Boolean completed
  ) {
    return ResponseEntity.ok(SuccessResponse.of(
        backofficeUserService.getUsers(keyword, luckyDrawEntered, completed)));
  }

  @Operation(
      summary = "참가자 상세 조회",
      description = "비밀번호는 응답하지 않습니다. (수정에서 재설정만 가능) 참가자가 아닌 계정은 404입니다.")
  @GetMapping("/{userId}")
  public ResponseEntity<SuccessResponse<UserDetailResponse>> getUser(@PathVariable Long userId) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeUserService.getUser(userId)));
  }

  @Operation(
      summary = "참가자 등록",
      description = "럭키드로우 응모·수료 여부는 false 로 등록됩니다. 이미 사용 중인 아이디면 409입니다.")
  @PostMapping
  public ResponseEntity<SuccessResponse<UserDetailResponse>> create(
      @Valid @RequestBody UserCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SuccessResponse.of(backofficeUserService.create(request)));
  }

  @Operation(
      summary = "참가자 수정 (SUPER_ADMIN 전용)",
      description = "전체 교체 방식이라 상세 조회 값으로 채운 폼 전체를 보냅니다. "
          + "비밀번호는 바꿀 때만 보내고, 바꾸지 않으면 password 를 빼거나 null 로 보냅니다. (빈 문자열은 400) "
          + "비밀번호를 바꾸면 해당 참가자는 로그아웃됩니다. "
          + "다른 회원이 쓰는 아이디면 409이므로, Swagger 예시의 loginId 는 대상 회원의 아이디로 바꿔서 테스트하세요. "
          + "럭키드로우 응모·수료 여부는 수정할 수 없습니다.")
  @PutMapping("/{userId}")
  public ResponseEntity<SuccessResponse<UserDetailResponse>> update(
      @PathVariable Long userId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    return ResponseEntity.ok(SuccessResponse.of(backofficeUserService.update(userId, request)));
  }

  @Operation(
      summary = "참가자 삭제 (SUPER_ADMIN 전용)",
      description = "되돌릴 수 없습니다. 참가자의 투표도 함께 삭제되어, 투표했던 프로젝트의 득표수가 줄어듭니다.")
  @DeleteMapping("/{userId}")
  public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long userId) {
    backofficeUserService.delete(userId);
    return ResponseEntity.ok(SuccessResponse.of("삭제되었습니다."));
  }
}
