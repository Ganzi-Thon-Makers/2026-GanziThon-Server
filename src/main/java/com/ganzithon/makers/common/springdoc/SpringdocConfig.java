package com.ganzithon.makers.common.springdoc;

import com.ganzithon.makers.common.dto.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {

  static {
    ModelResolver.enumsAsRef = true;
  }

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    Components components = new Components()
        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .description("JWT 토큰을 입력하세요 (Bearer prefix 제외)"));

    ModelConverters.getInstance()
        .read(ErrorResponse.class)
        .forEach(components::addSchemas);

    return new OpenAPI()
        .info(new Info()
            .title("Makers API Document")
            .version("v0.0.1")
            .description("Makers 서비스 API 명세서입니다."))
        .components(components)
        .addServersItem(new Server().url("/"))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
  }

  @Bean
  public GroupedOpenApi makersApi() {
    return GroupedOpenApi.builder()
        .group("Makers API")
        .pathsToMatch("/**")
        .build();
  }
}
