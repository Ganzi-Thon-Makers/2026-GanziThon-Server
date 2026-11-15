package com.ganzithon.makers.backoffice.image.controller;

import com.ganzithon.makers.backoffice.image.dto.ImageUploadResponse;
import com.ganzithon.makers.backoffice.image.storage.ImageStorage;
import com.ganzithon.makers.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 로컬에서 이미지 업로드를 확인하기 위한 임시 컨트롤러 (local 프로필 전용).
 * TODO: 프로젝트 등록 구현 할 때 삭제!
 */
@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/local/images")
public class LocalImageController {

  private final ImageStorage imageStorage;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<ImageUploadResponse>> upload(
      @RequestPart("file") MultipartFile file,
      @RequestParam("directory") String directory
  ) {
    String url = imageStorage.upload(file, directory);
    return ResponseEntity.ok(SuccessResponse.of(ImageUploadResponse.of(url)));
  }
}
