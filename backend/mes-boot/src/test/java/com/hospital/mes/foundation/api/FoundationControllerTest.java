package com.hospital.mes.foundation.api;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest(properties={"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration","mes.storage.endpoint=http://localhost:9000","mes.storage.access-key=test","mes.storage.secret-key=secret"})
@AutoConfigureMockMvc
class FoundationControllerTest {
 @Autowired MockMvc mvc;
 @Test void foundationStatusIsPublicAndTraced() throws Exception { mvc.perform(get("/api/v1/foundation/status").header("X-Trace-Id","test-trace")).andExpect(status().isOk()).andExpect(header().string("X-Trace-Id","test-trace")).andExpect(jsonPath("$.code").value("OK")).andExpect(jsonPath("$.traceId").value("test-trace")); }
 @Test void unknownApiIsProtected() throws Exception { mvc.perform(get("/api/v1/private")).andExpect(status().isForbidden()); }
 @Test void openApiDescribesFoundationEndpoint() throws Exception { mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("Hospital Pharmaceutical MES API")).andExpect(jsonPath("$.paths['/api/v1/foundation/status']").exists()); }
}
