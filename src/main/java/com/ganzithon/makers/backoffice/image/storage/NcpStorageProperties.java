package com.ganzithon.makers.backoffice.image.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ncp.storage")
public record NcpStorageProperties(
    String bucket,
    String publicBaseUrl
) {
}
