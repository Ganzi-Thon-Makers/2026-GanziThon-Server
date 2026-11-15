package com.ganzithon.makers.backoffice.image.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

  // 이미지를 검증한 뒤 {@code {directory}/{uuid}.{ext}} 키로 저장
  String upload(MultipartFile file, String directory);
  void delete(String url);
}
