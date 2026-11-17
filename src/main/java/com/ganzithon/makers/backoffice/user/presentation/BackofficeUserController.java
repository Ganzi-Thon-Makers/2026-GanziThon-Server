package com.ganzithon.makers.backoffice.user.presentation;

import com.ganzithon.makers.backoffice.user.application.BackofficeUserService;
import com.ganzithon.makers.backoffice.user.presentation.dto.UserListResponse;
import com.ganzithon.makers.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
