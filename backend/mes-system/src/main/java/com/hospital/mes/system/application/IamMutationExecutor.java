package com.hospital.mes.system.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.idempotency.IdempotencyCommand;
import com.hospital.mes.audit.idempotency.IdempotencyDecision;
import com.hospital.mes.audit.idempotency.IdempotencyDecisionType;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IamMutationExecutor {
    private final PlatformIdempotencyService idempotency;
    private final ObjectMapper json;

    public IamMutationExecutor(PlatformIdempotencyService idempotency, ObjectMapper json) {
        this.idempotency = idempotency;
        this.json = json;
    }

    @Transactional
    public <T> JsonNode execute(CurrentPlatformContext context, String operationCode,
                                String idempotencyKey, Object canonicalRequest,
                                String resourceType, Supplier<T> work,
                                Function<T, String> resourceId) {
        String requestJson = write(canonicalRequest);
        IdempotencyDecision decision = idempotency.begin(new IdempotencyCommand(
            context.organizationId(), context.actorId(), operationCode, idempotencyKey, requestJson));
        if (decision.type() == IdempotencyDecisionType.CONFLICT) {
            throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED",
                "Idempotency-Key was already used for a different request");
        }
        if (decision.type() == IdempotencyDecisionType.IN_PROGRESS_CONFLICT) {
            throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS",
                "An equivalent request is already in progress");
        }
        if (decision.type() == IdempotencyDecisionType.REPLAY) return read(decision.responseJson());

        T result = work.get();
        String responseJson = write(result);
        idempotency.complete(decision.handle(), 200, responseJson, resourceType, resourceId.apply(result));
        return read(responseJson);
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Request cannot be canonicalized", ex);
        }
    }

    private JsonNode read(String value) {
        try {
            return json.readTree(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored idempotency response is invalid", ex);
        }
    }
}
