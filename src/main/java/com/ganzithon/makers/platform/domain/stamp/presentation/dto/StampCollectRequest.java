package com.ganzithon.makers.platform.domain.stamp.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record StampCollectRequest(
    @NotBlank String token
) {}
