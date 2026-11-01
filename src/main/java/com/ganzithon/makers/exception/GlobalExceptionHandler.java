package com.ganzithon.makers.exception;

import java.util.List;

import com.ganzithon.makers.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.warn(
        "[BUSINESS-ERROR] type={} message={}",
        e.getErrorCode(),
        e.getMessage()
    );

    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(ErrorResponse.of(e.getMessage()));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request
  ) {
    List<ErrorResponse.FieldError> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> ErrorResponse.FieldError.of(
            error.getField(),
            error.getDefaultMessage()
        ))
        .toList();

    log.warn("[VALIDATION-ERROR] errors={}", errors);

    return ResponseEntity
        .status(ErrorCode.INVALID_INPUT.getStatus())
        .body(ErrorResponse.of(
            ErrorCode.INVALID_INPUT.getMessage(),
            errors
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("[UNEXPECTED-ERROR] message={}", e.getMessage(), e);

    return ResponseEntity
        .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ErrorResponse.of(
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        ));
  }
}
