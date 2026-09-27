package com.hospital.mes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "mes.storage.endpoint=http://localhost:9000",
    "mes.storage.access-key=test-access",
    "mes.storage.secret-key=test-secret"
})
class MesApplicationTest {
    @Test void contextStarts(ApplicationContext context) { assertNotNull(context); }
}
