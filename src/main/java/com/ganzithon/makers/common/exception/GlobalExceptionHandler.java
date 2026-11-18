package com.ganzithon.makers.common.exception;

import java.util.List;

import com.ganzithon.makers.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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
        .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
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
            ErrorCode.INVALID_INPUT,
            ErrorCode.INVALID_INPUT.getMessage(),
            errors
        ));
  }

  @Override
  protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request
  ) {
    log.warn(
        "[UPLOAD-SIZE-ERROR] maxUploadSize={} message={}",
        ex.getMaxUploadSize(),
        ex.getMessage()
    );

    return ResponseEntity
        .status(ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.getStatus())
        .body(ErrorResponse.of(ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED));
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request
  ) {
    log.warn(
        "[MVC-ERROR] type={} status={} message={}",
        ex.getClass().getSimpleName(),
        status,
        ex.getMessage()
    );

    return ResponseEntity
        .status(status)
        .body(ErrorResponse.of(errorCodeFor(status), status.value()));
  }

  private ErrorCode errorCodeFor(HttpStatusCode status) {
    HttpStatus resolved = HttpStatus.resolve(status.value());
    return switch (resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR) {
      case BAD_REQUEST -> ErrorCode.INVALID_INPUT;
      case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
      case FORBIDDEN -> ErrorCode.FORBIDDEN;
      case NOT_FOUND -> ErrorCode.NOT_FOUND;
      case METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED;
      default -> ErrorCode.REQUEST_PROCESS_FAILED;
    };
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("[UNEXPECTED-ERROR] message={}", e.getMessage(), e);

    return ResponseEntity
        .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
  }
}
