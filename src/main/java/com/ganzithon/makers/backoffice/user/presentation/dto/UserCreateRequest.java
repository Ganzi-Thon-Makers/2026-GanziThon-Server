package com.ganzithon.makers.backoffice.user.presentation.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// role 별 필수 항목은 서비스에서 검증 (UserFieldRules). 럭키드로우 응모·수료 여부는 받지 않음 (false 로 등록)
@Schema(description = """
    role 별 입력 항목 (필수 항목이 비어 있으면 400)
    - PARTICIPANT: loginId, password, name, school, phoneNumber, teamNumber, part 모두 필수
    - ADMIN: loginId, password, name, school, phoneNumber 필수 / part 선택 / teamNumber 는 받지 않음(무시)
    - TEAM: loginId, password, teamNumber 만 필수 / 나머지는 무시하고 이름은 "N팀", 학교·전화번호는 빈 값으로 저장
    - SUPER_ADMIN 은 생성할 수 없음 (400)""")
public record UserCreateRequest(
    @Schema(description = "PARTICIPANT, TEAM, ADMIN 중 하나 (등록 후 변경 불가)", example = "PARTICIPANT")
    @NotNull
    Role role,

    @Schema(description = "모든 role 필수. 영문, 숫자, _ 만 가능", example = "participant01")
    @NotBlank @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "영문, 숫자, _ 만 사용할 수 있습니다.")
    String loginId,

    // BCrypt 는 72바이트까지만 처리하므로 공백 없는 영문/숫자/특수문자(1글자 = 1바이트)로 제한
    @Schema(description = "모든 role 필수. 공백 없는 영문, 숫자, 특수문자", example = "password1234!")
    @NotBlank @Size(max = 72)
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "공백 없이 영문, 숫자, 특수문자만 사용할 수 있습니다.")
    String password,

    @Schema(description = "PARTICIPANT, ADMIN 필수 / TEAM 은 무시 (\"N팀\" 으로 저장)", example = "김서연")
    @Size(max = 50)
    String name,

    @Schema(description = "PARTICIPANT, ADMIN 필수 / TEAM 은 무시", example = "메이커스대학교")
    @Size(max = 50)
    String school,

    @Schema(description = "PARTICIPANT, ADMIN 필수 / TEAM 은 무시. 010-1234-5678 형식", example = "010-1234-5678")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "010-1234-5678 형식으로 입력해주세요.")
    String phoneNumber,

    // 행사 팀은 1~30팀 (팀 수가 바뀌면 여기와 수정 요청의 범위를 함께 변경)
    @Schema(description = "PARTICIPANT, TEAM 필수 (1~30) / ADMIN 은 무시", example = "1")
    @Min(value = 1, message = "팀 번호는 1~30 사이여야 합니다.")
    @Max(value = 30, message = "팀 번호는 1~30 사이여야 합니다.")
    Integer teamNumber,

    @Schema(description = "PARTICIPANT 필수 / ADMIN 선택 / TEAM 은 무시", example = "SERVER")
    Part part
) {
}
