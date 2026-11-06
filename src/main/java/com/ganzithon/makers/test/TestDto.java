package com.ganzithon.makers.test;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TestDto {

  @Schema(description = "테스트 생성 요청")
  public record CreateRequest(
      @Schema(description = "테스트 이름", example = "해커톤 1회차")
      @NotBlank(message = "이름은 필수입니다.")
      @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
      String name,

      @Schema(description = "상태", example = "OPEN")
      TestStatus status
  ) {
  }

  @Schema(description = "테스트 응답")
  public record Response(
      @Schema(description = "테스트 ID", example = "1")
      Long id,

      @Schema(description = "테스트 이름", example = "해커톤 1회차")
      String name,

      @Schema(description = "상태")
      TestStatus status
  ) {
  }
}
