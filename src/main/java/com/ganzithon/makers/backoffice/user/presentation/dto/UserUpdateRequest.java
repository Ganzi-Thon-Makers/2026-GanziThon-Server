package com.ganzithon.makers.backoffice.user.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 럭키드로우 응모·수료 여부는 수정 대상이 아니라 받지 않음. 검증 규칙은 등록과 같음
public record UserUpdateRequest(
    @Schema(description = "영문, 숫자, _ 만 가능. 바꾸지 않으려면 상세 조회의 loginId 를 그대로 보내세요.", example = "participant01")
    @NotBlank @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "영문, 숫자, _ 만 사용할 수 있습니다.")
    String loginId,

    // 생략(null)하면 기존 비밀번호 유지. 빈 문자열은 형식 오류
    @Schema(description = "바꿀 때만 입력. 바꾸지 않으려면 password 필드를 빼거나 null 로 보내세요. (빈 문자열은 400) "
        + "공백 없는 영문, 숫자, 특수문자", example = "newpassword1234!")
    @Size(max = 72)
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "공백 없이 영문, 숫자, 특수문자만 사용할 수 있습니다.")
    String password,

    @Schema(example = "김서연")
    @NotBlank @Size(max = 50)
    String name,

    @Schema(example = "메이커스대학교")
    @NotBlank @Size(max = 50)
    String school,

    @Schema(example = "010-1234-5678")
    @NotBlank
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "010-1234-5678 형식으로 입력해주세요.")
    String phoneNumber,

    // 행사 팀은 1~30팀 (팀 수가 바뀌면 여기와 등록 요청의 범위를 함께 변경)
    @Schema(description = "1~30", example = "1")
    @NotNull
    @Min(value = 1, message = "팀 번호는 1~30 사이여야 합니다.")
    @Max(value = 30, message = "팀 번호는 1~30 사이여야 합니다.")
    Integer teamNumber,

    @Schema(example = "SERVER")
    @NotNull
    Part part
) {
}
