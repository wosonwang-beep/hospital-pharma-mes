package com.hospital.mes.integration.storage;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
@Configuration @EnableConfigurationProperties(ObjectStorageProperties.class)
public class ObjectStorageConfiguration {
    @Bean MinioClient minioClient(ObjectStorageProperties p) { return MinioClient.builder().endpoint(p.endpoint()).credentials(p.accessKey(),p.secretKey()).build(); }
}
