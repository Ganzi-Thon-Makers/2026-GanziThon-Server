package com.ganzithon.makers.backoffice.image.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class NcpImageStorageTest {

  private static final String BUCKET = "test-bucket";
  private static final String BASE_URL = "https://kr.object.ncloudstorage.com/test-bucket";
  private static final String UUID_REGEX =
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

  private static final byte[] PNG_HEADER =
      {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};
  private static final byte[] JPEG_HEADER =
      {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1};
  private static final byte[] WEBP_HEADER =
      {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'E', 'B', 'P'};
  private static final byte[] GIF_HEADER = "GIF89a......".getBytes(StandardCharsets.US_ASCII);

  private S3Client s3Client;
  private NcpImageStorage storage;

  @BeforeEach
  void setUp() {
    s3Client = mock(S3Client.class);
    storage = createStorage(BASE_URL);
  }

  private NcpImageStorage createStorage(String baseUrl) {
    return new NcpImageStorage(s3Client, new NcpStorageProperties(BUCKET, baseUrl));
  }

  private static MockMultipartFile file(String filename, byte[] content) {
    return new MockMultipartFile("file", filename, "application/octet-stream", content);
  }

  private static MockMultipartFile file(String filename, byte[] header, int totalSize) {
    byte[] content = Arrays.copyOf(header, totalSize);
    return file(filename, content);
  }

  private static void assertErrorCode(ThrowingCallable callable, ErrorCode errorCode) {
    assertThatThrownBy(callable)
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(errorCode);
  }

  private PutObjectRequest capturePutRequest() {
    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
    return captor.getValue();
  }

  @Nested
  class Upload {

    @Test
    void 업로드_성공시_UUID_키로_저장하고_공개_URL을_반환한다() {
      MockMultipartFile png = file("my-thumbnail.png", PNG_HEADER, 1024);

      String url = storage.upload(png, "projects/2026/3");

      PutObjectRequest request = capturePutRequest();
      assertThat(request.bucket()).isEqualTo(BUCKET);
      assertThat(request.key()).matches("projects/2026/3/" + UUID_REGEX + "\\.png");
      assertThat(request.key()).doesNotContain("my-thumbnail");
      assertThat(request.contentType()).isEqualTo("image/png");
      assertThat(request.contentLength()).isEqualTo(1024L);
      assertThat(request.acl()).isEqualTo(ObjectCannedACL.PUBLIC_READ);
      assertThat(url).isEqualTo(BASE_URL + "/" + request.key());
    }

    @Test
    void 공개_읽기_ACL은_매번_항상_붙는다() {
      storage.upload(file("a.png", PNG_HEADER, 100), "projects");
      storage.upload(file("b.jpg", JPEG_HEADER, 100), "teams/1");
      storage.upload(file("c.webp", WEBP_HEADER, 100), "projects/2026/3");

      ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
      verify(s3Client, times(3)).putObject(captor.capture(), any(RequestBody.class));
      assertThat(captor.getAllValues())
          .extracting(PutObjectRequest::acl)
          .containsOnly(ObjectCannedACL.PUBLIC_READ);
    }

    @Test
    void 매번_다른_파일명을_생성한다() {
      String first = storage.upload(file("same.png", PNG_HEADER, 100), "projects");
      String second = storage.upload(file("same.png", PNG_HEADER, 100), "projects");

      assertThat(first).isNotEqualTo(second);
    }

    @ParameterizedTest
    @CsvSource({
        "photo.jpg,  jpg,  image/jpeg",
        "photo.JPEG, jpeg, image/jpeg",
        "photo.Png,  png,  image/png",
        "photo.webp, webp, image/webp",
    })
    void 확장자는_소문자로_정규화하고_Content_Type은_확장자_기준으로_저장한다(
        String filename, String expectedExtension, String expectedContentType) {
      byte[] header = switch (expectedExtension) {
        case "jpg", "jpeg" -> JPEG_HEADER;
        case "png" -> PNG_HEADER;
        default -> WEBP_HEADER;
      };
      // 클라이언트가 보낸 Content-Type은 무시된다
      MockMultipartFile multipartFile =
          new MockMultipartFile("file", filename, "text/plain", Arrays.copyOf(header, 64));

      storage.upload(multipartFile, "projects");

      PutObjectRequest request = capturePutRequest();
      assertThat(request.key()).endsWith("." + expectedExtension);
      assertThat(request.contentType()).isEqualTo(expectedContentType);
      assertThat(request.acl()).isEqualTo(ObjectCannedACL.PUBLIC_READ);
    }

    @Test
    void 기본_URL_끝의_슬래시는_정리된다() {
      storage = createStorage(BASE_URL + "/");

      String url = storage.upload(file("a.png", PNG_HEADER, 100), "projects");

      assertThat(url).startsWith(BASE_URL + "/projects/").doesNotContain("test-bucket//");
    }

    @Test
    void S3_호출이_실패하면_IMAGE_UPLOAD_FAILED() {
      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenThrow(SdkClientException.create("network error"));

      assertErrorCode(
          () -> storage.upload(file("a.png", PNG_HEADER, 100), "projects"),
          ErrorCode.IMAGE_UPLOAD_FAILED);
    }
  }

  @Nested
  class FileValidation {

    @Test
    void 빈_파일은_거부한다() {
      assertErrorCode(
          () -> storage.upload(file("a.png", new byte[0]), "projects"),
          ErrorCode.EMPTY_IMAGE_FILE);
      verifyNoInteractions(s3Client);
    }

    @Test
    void null_파일은_거부한다() {
      assertErrorCode(() -> storage.upload(null, "projects"), ErrorCode.EMPTY_IMAGE_FILE);
      verifyNoInteractions(s3Client);
    }

    @Test
    void 정책_상수는_5MB와_jpg_jpeg_png_webp다() {
      assertThat(ImageValidator.MAX_IMAGE_SIZE_BYTES).isEqualTo(5L * 1024 * 1024);
      assertThat(ImageFormat.ALLOWED_EXTENSIONS)
          .containsExactlyInAnyOrder("jpg", "jpeg", "png", "webp");
    }

    @Test
    void 최대_용량을_초과하면_거부한다() {
      int overSize = (int) ImageValidator.MAX_IMAGE_SIZE_BYTES + 1;

      assertErrorCode(
          () -> storage.upload(file("big.png", PNG_HEADER, overSize), "projects"),
          ErrorCode.IMAGE_SIZE_EXCEEDED);
      verifyNoInteractions(s3Client);
    }

    @Test
    void 최대_용량과_같으면_허용한다() {
      int exactSize = (int) ImageValidator.MAX_IMAGE_SIZE_BYTES;

      storage.upload(file("max.png", PNG_HEADER, exactSize), "projects");

      verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a.gif", "a.svg", "a.exe", "a.png.exe", "noext", "a.", ""})
    void 허용되지_않은_확장자는_거부한다(String filename) {
      assertErrorCode(
          () -> storage.upload(file(filename, PNG_HEADER, 100), "projects"),
          ErrorCode.INVALID_IMAGE_EXTENSION);
      verifyNoInteractions(s3Client);
    }

    @Test
    void 파일명이_null이면_거부한다() {
      MockMultipartFile noName =
          new MockMultipartFile("file", null, "image/png", Arrays.copyOf(PNG_HEADER, 100));

      assertErrorCode(
          () -> storage.upload(noName, "projects"),
          ErrorCode.INVALID_IMAGE_EXTENSION);
    }

    @Test
    void 확장자만_png이고_내용이_다른_포맷이면_거부한다() {
      MockMultipartFile disguised =
          new MockMultipartFile("file", "evil.png", "image/png", Arrays.copyOf(GIF_HEADER, 100));

      assertErrorCode(
          () -> storage.upload(disguised, "projects"),
          ErrorCode.INVALID_IMAGE_CONTENT);
      verifyNoInteractions(s3Client);
    }

    @Test
    void 확장자와_매직_바이트가_서로_다른_이미지_포맷이면_거부한다() {
      assertErrorCode(
          () -> storage.upload(file("photo.jpg", PNG_HEADER, 100), "projects"),
          ErrorCode.INVALID_IMAGE_CONTENT);
    }

    @Test
    void RIFF_헤더지만_WEBP가_아니면_거부한다() {
      byte[] wav = {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'A', 'V', 'E'};

      assertErrorCode(
          () -> storage.upload(file("sound.webp", wav, 100), "projects"),
          ErrorCode.INVALID_IMAGE_CONTENT);
    }

    @Test
    void 헤더보다_짧은_파일은_거부한다() {
      assertErrorCode(
          () -> storage.upload(file("tiny.png", new byte[]{(byte) 0x89, 0x50}), "projects"),
          ErrorCode.INVALID_IMAGE_CONTENT);
    }
  }

  @Nested
  class DirectoryValidation {

    @ParameterizedTest
    @CsvSource({
        "projects/2026/3,     projects/2026/3",
        "/projects/2026/3/,   projects/2026/3",
        "'  /projects//  ',   projects",
        "team_a/img-1,        team_a/img-1",
    })
    void 앞뒤_공백과_슬래시를_정리한다(String directory, String expected) {
      String url = storage.upload(file("a.png", PNG_HEADER, 100), directory);

      assertThat(capturePutRequest().key()).matches(Pattern.quote(expected) + "/" + UUID_REGEX + "\\.png");
      assertThat(url).startsWith(BASE_URL + "/" + expected + "/");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "   ", "/", "//",
        "..", "../etc", "projects/../secret", "projects/..", "a..b",
        "a//b", "a\\b", "a/./b", "a b", "프로젝트", "a?b", "a%2e%2e",
    })
    void 잘못된_경로는_거부한다(String directory) {
      assertErrorCode(
          () -> storage.upload(file("a.png", PNG_HEADER, 100), directory),
          ErrorCode.INVALID_IMAGE_DIRECTORY);
      verifyNoInteractions(s3Client);
    }
  }

  @Nested
  class Delete {

    @Test
    void URL에서_키를_추출해_삭제한다() {
      storage.delete(BASE_URL + "/projects/2026/3/abc.png");

      ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
      verify(s3Client).deleteObject(captor.capture());
      assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
      assertThat(captor.getValue().key()).isEqualTo("projects/2026/3/abc.png");
    }

    @Test
    void upload가_반환한_URL로_삭제할_수_있다() {
      String url = storage.upload(file("a.png", PNG_HEADER, 100), "projects");
      String uploadedKey = capturePutRequest().key();

      storage.delete(url);

      ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
      verify(s3Client).deleteObject(captor.capture());
      assertThat(captor.getValue().key()).isEqualTo(uploadedKey);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "https://evil.com/test-bucket/a.png",
        "https://kr.object.ncloudstorage.com/other-bucket/a.png",
        "https://kr.object.ncloudstorage.com/test-bucket-2/a.png",
        "https://kr.object.ncloudstorage.com/test-bucket",
        "https://kr.object.ncloudstorage.com/test-bucket/",
        "https://kr.object.ncloudstorage.com/test-bucket/../other/a.png",
    })
    void 저장소_URL이_아니면_거부한다(String url) {
      assertErrorCode(() -> storage.delete(url), ErrorCode.INVALID_IMAGE_URL);
      verifyNoInteractions(s3Client);
    }

    @Test
    void S3_호출이_실패하면_IMAGE_DELETE_FAILED() {
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenThrow(SdkClientException.create("network error"));

      assertErrorCode(
          () -> storage.delete(BASE_URL + "/projects/a.png"),
          ErrorCode.IMAGE_DELETE_FAILED);
    }
  }
}
