package com.ganzithon.makers.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class MaxUploadSizeExceptionHandlerTest {

  @RestController
  static class ThrowingController {

    @PostMapping("/upload")
    void upload() {
      throw new MaxUploadSizeExceededException(5 * 1024 * 1024);
    }
  }

  private final MockMvc mockMvc = MockMvcBuilders
      .standaloneSetup(new ThrowingController())
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();

  @Test
  void 업로드_용량_초과시_413과_ErrorResponse를_반환한다() throws Exception {
    mockMvc.perform(post("/upload"))
        .andExpect(status().is(ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.getStatus().value()))
        .andExpect(jsonPath("$.message").value(ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.getMessage()))
        .andExpect(jsonPath("$.errors").doesNotExist());
  }
}
