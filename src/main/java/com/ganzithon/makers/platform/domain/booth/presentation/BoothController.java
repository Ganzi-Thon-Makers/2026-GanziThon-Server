package com.ganzithon.makers.platform.domain.booth.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothDetailResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/booth")
@RequiredArgsConstructor
public class BoothController implements BoothApi {

  private final BoothService boothService;

  @Override
  @GetMapping
  public ResponseEntity<SuccessResponse<BoothListResponse>> getBoothsAll() {
    return ResponseEntity.ok(SuccessResponse.of(boothService.getBoothsAll()));
  }

  @Override
  @GetMapping("/{boothId}")
  public ResponseEntity<SuccessResponse<BoothDetailResponse>> getBoothDetail(
          @PathVariable Long boothId
  ) {
    return ResponseEntity.ok(SuccessResponse.of(boothService.getBoothDetail(boothId)));
  }
}
