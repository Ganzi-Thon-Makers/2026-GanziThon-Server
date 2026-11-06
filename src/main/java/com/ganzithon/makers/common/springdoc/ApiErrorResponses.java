package com.ganzithon.makers.common.springdoc;

import com.ganzithon.springdoc.ApiErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @ApiErrorResponse 를 여러 번 붙일 때 쓰이는 컨테이너 (직접 사용하지 않음) */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorResponses {

  ApiErrorResponse[] value();
}
