package com.ganzithon.springdoc;

import com.ganzithon.makers.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorResponses.class)
public @interface ApiErrorResponse {

  /** Swagger에 노출할 에러코드 */
  ErrorCode value();

  /** Example 설명 (비우면 생략) */
  String description() default "";
}
