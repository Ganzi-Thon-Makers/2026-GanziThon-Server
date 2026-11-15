package com.ganzithon.makers.backoffice.image.storage;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/** 업로드 이미지 파일, 저장 경로 검증 */
class ImageValidator {

  // 슬래시로 구분된 세그먼트, 각 세그먼트는 영문/숫자/-/_ 만 허용
  private static final Pattern DIRECTORY_PATTERN =
      Pattern.compile("^[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*$");

  // 업로드 가능한 최대 용량 (5MB)
  // spring.servlet.multipart.max-file-size 설정과 값을 맞출 것
  public static final long MAX_IMAGE_SIZE_BYTES = DataSize.ofMegabytes(5).toBytes();

  // 파일을 검증하고 -> 정규화된(소문자) 확장자 반환
  String validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.EMPTY_IMAGE_FILE);
    }
    if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
      throw new BusinessException(ErrorCode.IMAGE_SIZE_EXCEEDED);
    }

    String extension = extractExtension(file.getOriginalFilename());
    if (!ImageFormat.ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
    }
    ImageFormat format = ImageFormat.fromExtension(extension)
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION));

    if (!format.matches(readHeader(file))) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_CONTENT);
    }
    return extension;
  }

  // 앞뒤 공백/슬래시를 제거한 경로 반환
  String normalizeDirectory(String directory) {
    if (directory == null || directory.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_DIRECTORY);
    }
    String normalized = directory.strip().replaceAll("^/+|/+$", "");

    // 경로 탐색 문자열 거부
    if (normalized.contains("..") || !DIRECTORY_PATTERN.matcher(normalized).matches()) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_DIRECTORY);
    }
    return normalized;
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
    }
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
    }
    return originalFilename.substring(dotIndex + 1).strip().toLowerCase(Locale.ROOT);
  }

  private byte[] readHeader(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return inputStream.readNBytes(ImageFormat.HEADER_LENGTH);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
    }
  }
}
