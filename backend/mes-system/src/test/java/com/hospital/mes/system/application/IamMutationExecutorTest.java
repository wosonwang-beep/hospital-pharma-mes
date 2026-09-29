package com.hospital.mes.system.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.idempotency.IdempotencyDecision;
import com.hospital.mes.audit.idempotency.IdempotencyDecisionType;
import com.hospital.mes.audit.idempotency.IdempotencyHandle;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IamMutationExecutorTest {
    @Mock PlatformIdempotencyService idempotency;
    private final ObjectMapper json = new ObjectMapper();
    private final CurrentPlatformContext context = new CurrentPlatformContext(
        1, 7, Set.of("SYSTEM_ADMIN"), Set.of("iam:user:create"), "session", "request");

    @Test
    void ownsAndCompletesTheMutation() {
        when(idempotency.begin(any())).thenReturn(IdempotencyDecision.owner(new IdempotencyHandle(3, 0)));
        IamMutationExecutor executor = new IamMutationExecutor(idempotency, json);

        var result = executor.execute(context, "createUsers", "key-1", new Request("alice"),
            "USER", () -> new Result("11"), Result::id);

        assertThat(result.path("id").asText()).isEqualTo("11");
        verify(idempotency).complete(new IdempotencyHandle(3, 0), 200, "{\"id\":\"11\"}", "USER", "11");
    }

    @Test
    void replaysStoredResponseWithoutExecutingWork() {
        when(idempotency.begin(any())).thenReturn(new IdempotencyDecision(
            IdempotencyDecisionType.REPLAY, null, 200, "{\"id\":\"11\"}", "USER", "11"));
        IamMutationExecutor executor = new IamMutationExecutor(idempotency, json);

        var result = executor.execute(context, "createUsers", "key-1", new Request("alice"),
            "USER", () -> { throw new AssertionError("must not execute"); }, ignored -> "");

        assertThat(result.path("id").asText()).isEqualTo("11");
    }

    @Test
    void rejectsAnIdempotencyConflict() {
        when(idempotency.begin(any())).thenReturn(new IdempotencyDecision(
            IdempotencyDecisionType.CONFLICT, null, null, null, null, null));
        IamMutationExecutor executor = new IamMutationExecutor(idempotency, json);

        assertThrows(ResourceConflictException.class, () -> executor.execute(context,
            "createUsers", "key-1", new Request("alice"), "USER", () -> new Result("11"), Result::id));
    }

    @Test
    void returnsSensitiveOwnerResponseButPersistsOnlySanitizedReplay() {
        when(idempotency.begin(any())).thenReturn(IdempotencyDecision.owner(new IdempotencyHandle(4, 0)));
        IamMutationExecutor executor = new IamMutationExecutor(idempotency, json);

        var result = executor.execute(context, "createUsers", "key-2", new Request("alice"),
            "USER", () -> new SensitiveResult("11", "one-time-secret"), SensitiveResult::id,
            value -> new SensitiveResult(value.id(), null));

        assertThat(result.path("secret").asText()).isEqualTo("one-time-secret");
        verify(idempotency).complete(new IdempotencyHandle(4, 0), 200,
            "{\"id\":\"11\",\"secret\":null}", "USER", "11");
    }

    private record Request(String loginName) { }
    private record Result(String id) { }
    private record SensitiveResult(String id, String secret) { }
}
