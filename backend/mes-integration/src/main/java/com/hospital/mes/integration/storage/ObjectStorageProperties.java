package com.hospital.mes.integration.storage;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
@Validated @ConfigurationProperties("mes.storage")
public record ObjectStorageProperties(@NotBlank String endpoint, @NotBlank String accessKey, @NotBlank String secretKey) {}
