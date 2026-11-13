package com.ganzithon.makers.common.springdoc;

import com.ganzithon.makers.common.dto.ErrorResponse;
import com.ganzithon.makers.common.exception.ErrorCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class ErrorResponseCustomizer implements OperationCustomizer {

  private static final String JSON = "application/json";
  private static final String ERROR_SCHEMA_REF = "#/components/schemas/ErrorResponse";

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    Method method = handlerMethod.getMethod();
    Set<ApiErrorResponse> annotations = new LinkedHashSet<>(
        AnnotatedElementUtils.findMergedRepeatableAnnotations(method, ApiErrorResponse.class));

    Method interfaceMethod = ClassUtils.getInterfaceMethodIfPossible(method);
    if (interfaceMethod != method) {
      annotations.addAll(AnnotatedElementUtils
          .findMergedRepeatableAnnotations(interfaceMethod, ApiErrorResponse.class));
    }
    if (annotations.isEmpty()) {
      return operation;
    }

    ApiResponses responses = operation.getResponses();
    if (responses == null) {
      responses = new ApiResponses();
      operation.setResponses(responses);
    }

    Map<Integer, List<ApiErrorResponse>> byStatus = annotations.stream()
        .collect(Collectors.groupingBy(
            a -> a.value().getStatus().value(),
            TreeMap::new,
            Collectors.toList()));

    for (Map.Entry<Integer, List<ApiErrorResponse>> entry : byStatus.entrySet()) {
      int status = entry.getKey();
      String statusKey = String.valueOf(status);

      ApiResponse apiResponse = responses.get(statusKey);
      if (apiResponse == null) {
        apiResponse = new ApiResponse().description(reasonPhrase(status));
        responses.addApiResponse(statusKey, apiResponse);
      }

      Content content = apiResponse.getContent();
      if (content == null) {
        content = new Content();
        apiResponse.setContent(content);
      }

      MediaType mediaType = content.get(JSON);
      if (mediaType == null) {
        mediaType = new MediaType();
        content.addMediaType(JSON, mediaType);
      }
      if (mediaType.getSchema() == null) {
        mediaType.setSchema(new Schema<>().$ref(ERROR_SCHEMA_REF));
      }

      for (ApiErrorResponse annotation : entry.getValue()) {
        ErrorCode errorCode = annotation.value();
        mediaType.addExamples(errorCode.name(), toExample(errorCode, annotation.description()));
      }
    }

    return operation;
  }

  private Example toExample(ErrorCode errorCode, String description) {
    return new Example()
        .summary(errorCode.getMessage())
        .description(description.isBlank() ? null : description)
        .value(toExampleBody(errorCode));
  }

  private ErrorResponse toExampleBody(ErrorCode errorCode) {
    if (errorCode == ErrorCode.INVALID_INPUT) {
      return ErrorResponse.of(
          errorCode.getMessage(),
          List.of(ErrorResponse.FieldError.of("fieldName", "검증 실패 사유")));
    }
    return ErrorResponse.of(errorCode.getMessage());
  }

  private String reasonPhrase(int status) {
    HttpStatus httpStatus = HttpStatus.resolve(status);
    return httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
  }
}
