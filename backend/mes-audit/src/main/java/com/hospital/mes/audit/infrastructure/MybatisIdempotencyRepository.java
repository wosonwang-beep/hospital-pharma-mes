package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.audit.idempotency.IdempotencyHandle;
import com.hospital.mes.audit.idempotency.IdempotencyRecord;
import com.hospital.mes.audit.idempotency.IdempotencyRepository;
import com.hospital.mes.audit.idempotency.IdempotencyState;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class MybatisIdempotencyRepository implements IdempotencyRepository {
    private final IdempotencyMapper mapper;
    public MybatisIdempotencyRepository(IdempotencyMapper mapper) { this.mapper = mapper; }

    @Override public boolean claim(IdempotencyRecord r) {
        IdempotencyEntity e = new IdempotencyEntity();
        var now = java.time.LocalDateTime.now(ZoneOffset.UTC);
        e.setOrgId(r.organizationId()); e.setCreatedBy(r.actorId()); e.setCreatedAt(now);
        e.setUpdatedBy(r.actorId()); e.setUpdatedAt(now); e.setVersionNo(0L); e.setActorId(r.actorId());
        e.setOperationCode(r.operationCode()); e.setIdempotencyKey(r.idempotencyKey());
        e.setRequestDigest(r.requestDigest()); e.setState(r.state().name());
        e.setExpiresAt(r.expiresAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        try { return mapper.insert(e) == 1; } catch (DuplicateKeyException duplicate) { return false; }
    }

    @Override public IdempotencyRecord find(long org, long actor, String operation, String key) {
        IdempotencyEntity e = mapper.selectOne(new LambdaQueryWrapper<IdempotencyEntity>()
            .eq(IdempotencyEntity::getOrgId, org).eq(IdempotencyEntity::getActorId, actor)
            .eq(IdempotencyEntity::getOperationCode, operation).eq(IdempotencyEntity::getIdempotencyKey, key));
        if (e == null) throw new IllegalStateException("idempotency record disappeared");
        return new IdempotencyRecord(e.getId(), e.getOrgId(), e.getActorId(), e.getOperationCode(),
            e.getIdempotencyKey(), e.getRequestDigest(), IdempotencyState.valueOf(e.getState()), e.getHttpStatus(),
            e.getResponseJson(), e.getResourceType(), e.getResourceId(), e.getExpiresAt().toInstant(ZoneOffset.UTC),
            e.getVersionNo());
    }

    @Override public boolean complete(IdempotencyHandle h, int status, String response, String type, String id) {
        return mapper.complete(h.id(), h.versionNo(), status, response, type, id) == 1;
    }
}
