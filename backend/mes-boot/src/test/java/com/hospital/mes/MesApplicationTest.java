package com.hospital.mes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "mes.storage.endpoint=http://localhost:9000",
    "mes.storage.access-key=test-access",
    "mes.storage.secret-key=test-secret"
})
class MesApplicationTest {
    @MockitoBean JdbcTemplate jdbc;
    @Test void contextStarts(ApplicationContext context) { assertNotNull(context); }
}
