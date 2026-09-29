package com.hospital.mes.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.AuditEventQuery;
import com.hospital.mes.audit.application.AuditEventRepository;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class AuditContractTest {
    @Test
    void validatesRequiredEvidenceAndDigestFormat() {
        assertThatThrownBy(() -> new AuditCommand(1, 2, "ROLE", "UPDATE", "BATCH", "42",
            "not-a-digest", null, null, null, Instant.now(), "tx", "req", AuditSource.API, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuditCommand(0, 2, null, "UPDATE", "BATCH", "42",
            null, null, null, null, Instant.now(), "tx", null, AuditSource.API, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void appendRequiresAnExistingTransactionAndRepositoryIsAppendOnly() throws Exception {
        Method append = AuditApplicationService.class.getMethod("append", AuditCommand.class);
        Transactional transaction = append.getAnnotation(Transactional.class);
        assertThat(transaction).isNotNull();
        assertThat(transaction.propagation()).isEqualTo(Propagation.MANDATORY);
        assertThat(AuditEventRepository.class.getMethods())
            .extracting(Method::getName)
            .contains("append", "query")
            .doesNotContain("update", "delete", "remove");
    }

    @Test
    void queryRequiresPairedObjectFilterAndBoundedPageSize() {
        assertThatThrownBy(() -> new AuditEventQuery(null, null, null, "42", null, null, null,
            null, null, 0, 50)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuditEventQuery(null, null, null, null, null, null, null,
            null, null, 0, 201)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuditEventQuery(null, null, null, null, null, null, null,
            Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"), 0, 50))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
