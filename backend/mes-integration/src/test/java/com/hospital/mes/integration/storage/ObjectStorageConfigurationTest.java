package com.hospital.mes.integration.storage;
import static org.junit.jupiter.api.Assertions.*;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
class ObjectStorageConfigurationTest {
    @Test void createsClientForValidSettings(){ var p=new ObjectStorageProperties("http://localhost:9000","access","secret123"); MinioClient c=new ObjectStorageConfiguration().minioClient(p); assertNotNull(c); }
}
