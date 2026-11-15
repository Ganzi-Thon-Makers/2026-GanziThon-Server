package com.ganzithon.makers.backoffice.image.dto;

public record ImageUploadResponse(String url) {

  public static ImageUploadResponse of(String url) {
    return new ImageUploadResponse(url);
  }
}
