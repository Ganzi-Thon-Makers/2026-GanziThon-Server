package com.ganzithon.makers.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 메서드입니다."),
  MAX_UPLOAD_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "업로드 가능한 최대 용량을 초과했습니다."),
  REQUEST_PROCESS_FAILED(HttpStatus.BAD_REQUEST, "요청을 처리할 수 없습니다."),

  // Image
  EMPTY_IMAGE_FILE(HttpStatus.BAD_REQUEST, "이미지 파일이 비어 있습니다."),
  INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 이미지 확장자입니다."),
  INVALID_IMAGE_CONTENT(HttpStatus.BAD_REQUEST, "이미지 파일 내용이 확장자와 일치하지 않습니다."),
  IMAGE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "이미지 파일 용량이 너무 큽니다."),
  INVALID_IMAGE_DIRECTORY(HttpStatus.BAD_REQUEST, "이미지 저장 경로가 올바르지 않습니다."),
  INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "이미지 URL이 올바르지 않습니다."),
  IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
  IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),

  // Project
  PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
  DUPLICATE_PROJECT_MEMBER(HttpStatus.BAD_REQUEST, "같은 회원이 팀원으로 중복 포함되어 있습니다."),
  PROJECT_MEMBER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 다른 프로젝트에 소속된 회원입니다."),
  DUPLICATE_TEAM_NUMBER(HttpStatus.CONFLICT, "이미 다른 프로젝트가 사용 중인 팀 번호입니다."),
  INVALID_PROJECT_MEMBER_ROLE(HttpStatus.BAD_REQUEST, "참가자 또는 팀 계정만 팀원으로 등록할 수 있습니다."),
  MULTIPLE_TEAM_ACCOUNTS(HttpStatus.BAD_REQUEST, "팀 계정은 프로젝트당 하나만 등록할 수 있습니다."),

  // User
  DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
  INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "SUPER_ADMIN 은 사용할 수 없는 역할입니다."),
  MISSING_REQUIRED_USER_FIELD(HttpStatus.BAD_REQUEST, "역할에 필요한 항목이 비어 있습니다."),

  // Booth
  BOOTH_NOT_FOUND(HttpStatus.NOT_FOUND, "부스를 찾을 수 없습니다."),
  PROJECT_ALREADY_ASSIGNED_TO_BOOTH(HttpStatus.CONFLICT, "이미 다른 부스에 배정된 프로젝트입니다."),

  // Timetable
  TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."),
  INVALID_TIMETABLE_TIME_RANGE(HttpStatus.BAD_REQUEST, "종료시간은 시작시간보다 늦어야 합니다."),
  TIMETABLE_TIME_OVERLAP(HttpStatus.CONFLICT, "다른 일정과 시간이 겹칩니다."),
  VOTE_TIMETABLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "투표 일정은 하나만 등록할 수 있습니다."),
  VOTE_TIMETABLE_NOT_DELETABLE(HttpStatus.CONFLICT, "투표 일정은 삭제할 수 없습니다."),
  VOTE_TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "투표 일정이 등록되지 않았습니다."),
  VOTE_IN_PROGRESS(HttpStatus.CONFLICT, "투표가 진행 중이라 시간을 수정할 수 없습니다. 일시중지 후 수정해 주세요."),

  // Vote
  VOTE_PARTICIPANT_ONLY(HttpStatus.FORBIDDEN, "투표는 참가자만 할 수 있습니다."),
  VOTE_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "현재 투표 시간이 아닙니다."),
  VOTE_ALREADY_CAST(HttpStatus.CONFLICT, "이미 투표를 완료했습니다."),
  VOTE_OWN_PROJECT(HttpStatus.BAD_REQUEST, "본인 팀 프로젝트에는 투표할 수 없습니다."),

  // Stamp / QR Token
  QR_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 QR 코드입니다. 부스에 표시된 QR 코드를 다시 확인해주세요."),
  QR_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 QR 코드입니다. 부스 운영자에게 새 QR 발급을 요청해주세요."),
  STAMP_ALREADY_COLLECTED(HttpStatus.CONFLICT, "해당 부스의 스탬프는 이미 적립되어 추가 적립할 수 없습니다."),
  STAMP_OWN_BOOTH(HttpStatus.BAD_REQUEST, "본인 팀 부스의 스탬프는 적립할 수 없습니다."),
  STAMP_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "직전 스탬프 적립 후 5분이 지나야 다음 스탬프를 받을 수 있습니다."),
  STAMP_PARTICIPANT_ONLY(HttpStatus.FORBIDDEN, "스탬프는 일반 참가자만 받을 수 있습니다. 팀 계정과 관리자 계정은 받을 수 없습니다."),

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

  //User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
  LOGGED_IN_ELSEWHERE(HttpStatus.UNAUTHORIZED, "다른 기기에서 로그인되어 로그아웃되었습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
