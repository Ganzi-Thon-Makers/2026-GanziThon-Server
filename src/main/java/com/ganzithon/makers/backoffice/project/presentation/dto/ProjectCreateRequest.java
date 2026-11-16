package com.ganzithon.makers.backoffice.project.presentation.dto;

import com.ganzithon.makers.platform.domain.project.domain.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProjectCreateRequest(
    @Schema(example = "반차 — 직장인 반려동물 돌봄 매칭")
    @NotBlank @Size(max = 100)
    String title,

    @Schema(description = "선택", example = "https://bancha.ganzithon.com")
    @Size(max = 500)
    String serviceUrl,

    @Schema(example = "댕댕이지키미")
    @NotBlank @Size(max = 100)
    String teamName,

    @Schema(description = "팀원 최종 목록 (비어 있어도 됨)")
    @Valid
    List<ProjectMemberRequest> members,

    @Schema(example = "React, TypeScript, Spring Boot 3, MySQL 8, NCP Object Storage, Kakao Map API")
    @NotBlank @Size(max = 500)
    String techStack,

    @Schema(example = "혼자 사는 직장인의 반려동물을 같은 동네 이웃과 돌봄 매칭해주는 플랫폼")
    @NotBlank @Size(max = 200)
    String summary,

    @Schema(example = "야근이나 갑작스러운 출장으로 반려동물을 혼자 둬야 할 때, 인증된 같은 동네 이웃에게 "
        + "산책·급식을 맡길 수 있는 돌봄 매칭 서비스입니다. 위치 기반 매칭, 돌봄 일지 사진 전송, "
        + "실시간 산책 경로 공유 기능을 제공하며, 돌봄 완료 후 크레딧으로 보상받아 "
        + "다음에 내 강아지를 맡길 때 사용할 수 있습니다.")
    @NotBlank
    String description,

    @Schema(example = "WEB")
    @NotNull
    Platform platform
) {

  public List<ProjectMemberRequest> membersOrEmpty() {
    return members == null ? List.of() : members;
  }
}
