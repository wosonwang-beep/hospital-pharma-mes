package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.mes.audit.application.AuditEventPage;
import com.hospital.mes.audit.application.AuditEventQuery;
import com.hospital.mes.audit.application.AuditEventRepository;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.audit.domain.AuditEvent;
import com.hospital.mes.audit.domain.AuditSource;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class MybatisAuditEventRepository implements AuditEventRepository {
    private final AuditEventMapper mapper;

    public MybatisAuditEventRepository(AuditEventMapper mapper) { this.mapper = mapper; }

    @Override public void append(AuditCommand c) {
        AuditEventEntity e = new AuditEventEntity();
        e.setOrgId(c.organizationId()); e.setCreatedBy(c.actorId());
        e.setCreatedAt(c.occurredAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        e.setActorId(c.actorId()); e.setActorRole(c.actorRole()); e.setAction(c.action());
        e.setObjectType(c.objectType()); e.setObjectId(c.objectId()); e.setOldValueDigest(c.oldValueDigest());
        e.setNewValueDigest(c.newValueDigest()); e.setReason(c.reason()); e.setClientInfo(c.clientInfo());
        e.setOccurredAt(c.occurredAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        e.setTransactionId(c.transactionId()); e.setRequestId(c.requestId()); e.setSource(c.source().name());
        e.setIdempotencyKey(c.idempotencyKey());
        if (mapper.insert(e) != 1) throw new IllegalStateException("audit event was not appended");
    }

    @Override public AuditEventPage query(long orgId, AuditEventQuery q) {
        LambdaQueryWrapper<AuditEventEntity> w = new LambdaQueryWrapper<AuditEventEntity>()
            .eq(AuditEventEntity::getOrgId, orgId)
            .eq(q.actorId() != null, AuditEventEntity::getActorId, q.actorId())
            .eq(q.action() != null, AuditEventEntity::getAction, q.action())
            .eq(q.objectType() != null, AuditEventEntity::getObjectType, q.objectType())
            .eq(q.objectId() != null, AuditEventEntity::getObjectId, q.objectId())
            .eq(q.source() != null, AuditEventEntity::getSource, q.source() == null ? null : q.source().name())
            .eq(q.transactionId() != null, AuditEventEntity::getTransactionId, q.transactionId())
            .eq(q.requestId() != null, AuditEventEntity::getRequestId, q.requestId())
            .ge(q.occurredFrom() != null, AuditEventEntity::getOccurredAt,
                q.occurredFrom() == null ? null : q.occurredFrom().atOffset(ZoneOffset.UTC).toLocalDateTime())
            .lt(q.occurredTo() != null, AuditEventEntity::getOccurredAt,
                q.occurredTo() == null ? null : q.occurredTo().atOffset(ZoneOffset.UTC).toLocalDateTime())
            .orderByDesc(AuditEventEntity::getOccurredAt).orderByDesc(AuditEventEntity::getId);
        Page<AuditEventEntity> p = mapper.selectPage(Page.of(q.page() + 1L, q.size()), w);
        return new AuditEventPage(p.getRecords().stream().map(MybatisAuditEventRepository::domain).toList(),
            q.page(), q.size(), p.getTotal());
    }

    private static AuditEvent domain(AuditEventEntity e) {
        return new AuditEvent(e.getId(), e.getOrgId(), e.getActorId(), e.getActorRole(), e.getAction(),
            e.getObjectType(), e.getObjectId(), e.getOldValueDigest(), e.getNewValueDigest(), e.getReason(),
            e.getClientInfo(), e.getOccurredAt().toInstant(ZoneOffset.UTC), e.getTransactionId(), e.getRequestId(),
            AuditSource.valueOf(e.getSource()));
    }
}
