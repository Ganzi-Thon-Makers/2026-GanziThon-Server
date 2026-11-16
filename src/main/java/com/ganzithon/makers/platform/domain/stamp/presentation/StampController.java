package com.ganzithon.makers.platform.domain.stamp.presentation;

import com.ganzithon.makers.common.auth.CurrentUserProvider;
import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.platform.domain.stamp.application.StampFacade;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampBoardResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectRequest;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stamps")
@RequiredArgsConstructor
public class StampController {

  private final StampFacade stampFacade;

  @PostMapping
  public ResponseEntity<SuccessResponse<StampCollectResponse>> collect(
      @RequestParam Long userId,
      @Valid @RequestBody StampCollectRequest requestBody) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.collect(userId, requestBody.token())));
  }

  @GetMapping
  public ResponseEntity<SuccessResponse<StampBoardResponse>> getBoard(
      @RequestParam Long userId) {
    return ResponseEntity.ok(SuccessResponse.of(stampFacade.getBoard(userId)));
  }
}
