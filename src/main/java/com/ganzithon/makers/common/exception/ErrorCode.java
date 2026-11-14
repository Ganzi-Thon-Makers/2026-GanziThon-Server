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

  // Image
  EMPTY_IMAGE_FILE(HttpStatus.BAD_REQUEST, "이미지 파일이 비어 있습니다."),
  INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 이미지 확장자입니다."),
  INVALID_IMAGE_CONTENT(HttpStatus.BAD_REQUEST, "이미지 파일 내용이 확장자와 일치하지 않습니다."),
  IMAGE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "이미지 파일 용량이 너무 큽니다."),
  INVALID_IMAGE_DIRECTORY(HttpStatus.BAD_REQUEST, "이미지 저장 경로가 올바르지 않습니다."),
  INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "이미지 URL이 올바르지 않습니다."),
  IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
  IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
