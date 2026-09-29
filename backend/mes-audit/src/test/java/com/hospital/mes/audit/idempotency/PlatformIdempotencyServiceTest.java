package com.hospital.mes.audit.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlatformIdempotencyServiceTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final PlatformIdempotencyService service = new PlatformIdempotencyService(repository,
        Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void grantsOneOwnerThenReportsInProgressForSameRequest() {
        IdempotencyCommand command = new IdempotencyCommand(1, 2, "SIGN_RECORD", "key-1", "{\"a\":1}");
        assertThat(service.begin(command).type()).isEqualTo(IdempotencyDecisionType.OWNER);
        assertThat(service.begin(command).type()).isEqualTo(IdempotencyDecisionType.IN_PROGRESS_CONFLICT);
    }

    @Test
    void replaysCompletedResponseAndRejectsChangedRequest() {
        IdempotencyCommand first = new IdempotencyCommand(1, 2, "SIGN_RECORD", "key-1", "{\"a\":1}");
        IdempotencyDecision owner = service.begin(first);
        service.complete(owner.handle(), 200, "{\"ok\":true}", "SIGNATURE", "9");
        IdempotencyDecision replay = service.begin(first);
        assertThat(replay.type()).isEqualTo(IdempotencyDecisionType.REPLAY);
        assertThat(replay.httpStatus()).isEqualTo(200);
        assertThat(service.begin(new IdempotencyCommand(1, 2, "SIGN_RECORD", "key-1", "different")).type())
            .isEqualTo(IdempotencyDecisionType.CONFLICT);
    }

    private static final class MemoryRepository implements IdempotencyRepository {
        private final Map<String, IdempotencyRecord> records = new HashMap<>();
        private long sequence;
        @Override public boolean claim(IdempotencyRecord record) {
            String key = key(record);
            if (records.containsKey(key)) return false;
            records.put(key, new IdempotencyRecord(++sequence, record.organizationId(), record.actorId(),
                record.operationCode(), record.idempotencyKey(), record.requestDigest(), record.state(),
                record.httpStatus(), record.responseJson(), record.resourceType(), record.resourceId(),
                record.expiresAt(), record.versionNo()));
            return true;
        }
        @Override public IdempotencyRecord find(long org, long actor, String operation, String key) {
            return records.get(org + ":" + actor + ":" + operation + ":" + key);
        }
        @Override public boolean complete(IdempotencyHandle h, int status, String response, String type, String id) {
            IdempotencyRecord old = records.values().stream().filter(r -> r.id() == h.id()).findFirst().orElseThrow();
            records.put(key(old), old.completed(status, response, type, id));
            return true;
        }
        private static String key(IdempotencyRecord r) {
            return r.organizationId() + ":" + r.actorId() + ":" + r.operationCode() + ":" + r.idempotencyKey();
        }
    }
}
