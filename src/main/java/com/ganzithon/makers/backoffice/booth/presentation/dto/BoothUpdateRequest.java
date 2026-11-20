package com.ganzithon.makers.backoffice.booth.presentation.dto;

import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 전체 교체. 상세(목록) 값으로 채운 폼 전체를 보냄
@Schema(description = "부스 정보 전체 교체. 바꾸지 않는 항목도 현재 값을 그대로 보냅니다.")
public record BoothUpdateRequest(
    @Schema(description = "구역", example = "ZONE2")
    @NotNull
    Zone zone,

    @Schema(description = "호수", example = "101호")
    @NotBlank
    @Size(max = 20)
    String roomNumber,

    @Schema(description = "부스 번호. 같은 구역 안에서 중복 불가", example = "7")
    @NotNull
    @Min(1)
    Integer boothNumber,

    @Schema(description = "위치 설명 (선택, 최대 100자). 비우면 null 로 저장", example = "입구 왼쪽 첫번째 부스")
    @Size(max = 100)
    String location,

    @Schema(description = "배정할 프로젝트 id. null 이면 배정 취소", example = "1")
    Long projectId
) {
}
