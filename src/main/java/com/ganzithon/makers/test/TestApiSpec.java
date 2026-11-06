package com.ganzithon.makers.test;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.common.springdoc.ApiErrorResponse;
import com.ganzithon.makers.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Test", description = "Swagger 설정 테스트용 API")
public interface TestApiSpec {

  @Operation(summary = "테스트 단건 조회", description = "id=1만 조회됩니다. 그 외에는 404가 발생합니다.")
  @ApiErrorResponse(ErrorCode.NOT_FOUND)
  SuccessResponse<TestDto.Response> get(
      @Parameter(description = "테스트 ID", example = "1") Long id);

  @Operation(summary = "테스트 목록 조회", description = "status로 필터링합니다. (생략 시 전체)")
  SuccessResponse<List<TestDto.Response>> list(
      @Parameter(description = "상태 필터") TestStatus status);

  @Operation(summary = "테스트 생성", description = "name이 비어있거나 20자를 넘으면 400이 발생합니다.")
  @ApiErrorResponse(value = ErrorCode.INVALID_INPUT, description = "@Valid 검증 실패")
  SuccessResponse<TestDto.Response> create(TestDto.CreateRequest request);

  @Operation(summary = "테스트 삭제", description = "id=1은 삭제 권한이 없어 403, id=2 이상은 404입니다.")
  @ApiErrorResponse(ErrorCode.FORBIDDEN)
  @ApiErrorResponse(ErrorCode.NOT_FOUND)
  SuccessResponse<Void> delete(
      @Parameter(description = "테스트 ID", example = "1") Long id);
}
