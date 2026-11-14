package com.ganzithon.makers.backoffice.image.storage;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 지원하는 이미지 포맷과 확장자, 저장 Content-Type, 파일 시그니처(매직 바이트) 매핑.
 */
enum ImageFormat {

  JPEG(List.of("jpg", "jpeg"), "image/jpeg") {
    @Override
    boolean matches(byte[] header) {
      return startsWith(header, 0, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }
  },
  PNG(List.of("png"), "image/png") {
    @Override
    boolean matches(byte[] header) {
      return startsWith(header, 0,
          new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    }
  },
  WEBP(List.of("webp"), "image/webp") {
    @Override
    boolean matches(byte[] header) {
      // RIFF????WEBP
      return startsWith(header, 0, ascii("RIFF")) && startsWith(header, 8, ascii("WEBP"));
    }
  },
  ;

  /** 시그니처 판별에 필요한 헤더 길이 */
  static final int HEADER_LENGTH = 12;

  /** 업로드 허용 확장자 (소문자). 각 포맷의 확장자 정의로부터 만든다. */
  public static final Set<String> ALLOWED_EXTENSIONS = Arrays.stream(values())
      .flatMap(format -> format.extensions.stream())
      .collect(Collectors.toUnmodifiableSet());

  private final List<String> extensions;
  private final String contentType;

  ImageFormat(List<String> extensions, String contentType) {
    this.extensions = extensions;
    this.contentType = contentType;
  }

  abstract boolean matches(byte[] header);

  String contentType() {
    return contentType;
  }

  static Optional<ImageFormat> fromExtension(String extension) {
    return Arrays.stream(values())
        .filter(format -> format.extensions.contains(extension))
        .findFirst();
  }

  private static boolean startsWith(byte[] source, int offset, byte[] prefix) {
    if (source.length < offset + prefix.length) {
      return false;
    }
    return Arrays.equals(source, offset, offset + prefix.length, prefix, 0, prefix.length);
  }

  private static byte[] ascii(String value) {
    return value.getBytes(StandardCharsets.US_ASCII);
  }
}
