package com.ganzithon.makers.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ganzithon.makers.common.exception.ErrorCode;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private final int status;
  private final String code;
  private final String message;
  private final List<FieldError> errors;

  @Builder(access = AccessLevel.PRIVATE)
  private ErrorResponse(int status, String code, String message, List<FieldError> errors) {
    this.status = status;
    this.code = code;
    this.message = message;
    this.errors = errors;
  }

  public static ErrorResponse of(ErrorCode errorCode) {
    return of(errorCode, errorCode.getStatus().value(), errorCode.getMessage(), null);
  }

  public static ErrorResponse of(ErrorCode errorCode, String message) {
    return of(errorCode, errorCode.getStatus().value(), message, null);
  }

  public static ErrorResponse of(ErrorCode errorCode, int status) {
    return of(errorCode, status, errorCode.getMessage(), null);
  }

  public static ErrorResponse of(
      ErrorCode errorCode,
      String message,
      List<FieldError> errors
  ) {
    return of(errorCode, errorCode.getStatus().value(), message, errors);
  }

  private static ErrorResponse of(
      ErrorCode errorCode,
      int status,
      String message,
      List<FieldError> errors
  ) {
    return ErrorResponse.builder()
        .status(status)
        .code(errorCode.name())
        .message(message)
        .errors(errors)
        .build();
  }

  @Getter
  public static class FieldError {

    private final String field;
    private final String reason;

    @Builder(access = AccessLevel.PRIVATE)
    private FieldError(String field, String reason) {
      this.field = field;
      this.reason = reason;
    }

    public static FieldError of(String field, String reason) {
      return FieldError.builder()
          .field(field)
          .reason(reason)
          .build();
    }
  }
}
