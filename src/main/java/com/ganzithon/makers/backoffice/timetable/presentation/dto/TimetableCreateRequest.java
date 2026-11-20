package com.ganzithon.makers.backoffice.timetable.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TimetableCreateRequest(

    // 원시 boolean 이면 필드를 생략했을 때 역직렬화가 실패해(Jackson 3 기본 설정) 래퍼 타입으로 받고 null 은 false 로 바꿈
    @Schema(description = "투표 일정 여부 (생략 시 false). 투표 일정은 하나만 등록 가능", example = "false")
    Boolean isVote,

    @Schema(description = "제목", example = "오프닝 및 오리엔테이션")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    String title,

    @Schema(description = "상세 내용", example = "행사 진행 방식과 전체 일정을 안내합니다.", nullable = true)
    String description,

    @Schema(description = "시작시간", example = "2026-11-21T10:30:00")
    @NotNull(message = "시작시간은 필수입니다.")
    LocalDateTime startTime,

    @Schema(description = "종료시간 (시작시간보다 늦어야 함)", example = "2026-11-21T11:00:00")
    @NotNull(message = "종료시간은 필수입니다.")
    LocalDateTime endTime
) {

  public TimetableCreateRequest {
    if (isVote == null) {
      isVote = false;
    }
  }
}
