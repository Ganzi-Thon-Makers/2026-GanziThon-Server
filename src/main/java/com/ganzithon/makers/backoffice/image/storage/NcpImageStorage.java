package com.ganzithon.makers.backoffice.image.storage;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@EnableConfigurationProperties(NcpStorageProperties.class)
public class NcpImageStorage implements ImageStorage {

  // 업로드 객체에 항상 부여하는 ACL (공개 URL로 접근 가능하도록)
  private static final ObjectCannedACL UPLOAD_OBJECT_ACL = ObjectCannedACL.PUBLIC_READ;

  private final S3Client s3Client;
  private final ImageValidator validator;
  private final String bucket;
  private final String publicBaseUrl;

  public NcpImageStorage(S3Client s3Client, NcpStorageProperties ncpProperties) {
    this.s3Client = s3Client;
    this.validator = new ImageValidator();
    this.bucket = ncpProperties.bucket();
    this.publicBaseUrl = ncpProperties.publicBaseUrl().replaceAll("/+$", "");
  }

  @Override
  public String upload(MultipartFile file, String directory) {
    String normalizedDirectory = validator.normalizeDirectory(directory);
    String extension = validator.validate(file);
    ImageFormat format = ImageFormat.fromExtension(extension).orElseThrow();
    String key = normalizedDirectory + "/" + UUID.randomUUID() + "." + extension;

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(format.contentType())
        .contentLength(file.getSize())
        .acl(UPLOAD_OBJECT_ACL)
        .build();

    try (InputStream inputStream = file.getInputStream()) {
      s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
    } catch (IOException | SdkException e) {
      log.error("[IMAGE-UPLOAD-FAILED] key={} message={}", key, e.getMessage(), e);
      throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
    }

    return publicBaseUrl + "/" + key;
  }

  @Override
  public void delete(String url) {
    String key = extractKey(url);

    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());
    } catch (SdkException e) {
      log.error("[IMAGE-DELETE-FAILED] key={} message={}", key, e.getMessage(), e);
      throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED);
    }
  }

  private String extractKey(String url) {
    String prefix = publicBaseUrl + "/";
    if (url == null || !url.startsWith(prefix)) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
    }
    String key = url.substring(prefix.length());
    if (key.isBlank() || key.contains("..")) {
      throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
    }
    return key;
  }
}
