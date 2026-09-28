package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.identity.LoginIdentity;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.identity.SecurityEvent;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class SystemIdentityDirectory implements IdentityDirectory {
    private final SysUserMapper users;
    private final SysRolePermissionMapper rolePermissions;
    private final SysSecurityEventMapper events;

    public SystemIdentityDirectory(SysUserMapper users, SysRolePermissionMapper rolePermissions,
                                   SysSecurityEventMapper events) {
        this.users = users;
        this.rolePermissions = rolePermissions;
        this.events = events;
    }

    @Override
    public Optional<LoginIdentity> findForLogin(String loginName) {
        if (loginName == null || loginName.isBlank()) return Optional.empty();
        String normalized = Normalizer.normalize(loginName.strip(), Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
        SysUserEntity user = users.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getLoginNameNormalized, normalized));
        return Optional.ofNullable(user).map(this::toIdentity);
    }

    @Override
    @Transactional
    public void recordLoginFailure(long userId, Instant now) {
        updateCounter(userId, now, false);
    }

    @Override
    @Transactional
    public void recordLoginSuccess(long userId, Instant now) {
        updateCounter(userId, now, true);
    }

    private void updateCounter(long userId, Instant now, boolean success) {
        for (int attempt = 0; attempt < 16; attempt++) {
            SysUserEntity current = users.selectById(userId);
            if (current == null) throw new IllegalArgumentException("Unknown user");
            if (!success && current.getLockedUntil() != null
                && !current.getLockedUntil().isAfter(LocalDateTime.ofInstant(now, ZoneOffset.UTC))) {
                users.resetExpiredLock(userId, current.getVersion(),
                    LocalDateTime.ofInstant(now, ZoneOffset.UTC));
                continue;
            }
            int updated = success
                ? users.recordSuccess(userId, current.getVersion())
                : users.recordFailure(userId, current.getVersion(),
                    LocalDateTime.ofInstant(now.plusSeconds(15 * 60), ZoneOffset.UTC));
            if (updated == 1) return;
        }
        throw new OptimisticLockingFailureException("Concurrent login update conflict");
    }

    @Override
    @Transactional(readOnly = true)
    public LoginSnapshot loadLoginSnapshot(long userId) {
        SysUserEntity user = users.selectById(userId);
        if (user == null) throw new IllegalArgumentException("Unknown user");
        return new LoginSnapshot(userId, user.getLoginName(), user.getDisplayName(),
            Set.copyOf(rolePermissions.activeRoleCodes(userId)),
            Set.copyOf(rolePermissions.activePermissionCodes(userId)),
            Boolean.TRUE.equals(user.getMustChangePassword()));
    }

    @Override
    @Transactional
    public void appendSecurityEvent(SecurityEvent event) {
        SysSecurityEventEntity entity = new SysSecurityEventEntity();
        entity.setEventType(event.eventType());
        entity.setOutcome(event.outcome());
        entity.setActorUserId(event.actorUserId());
        entity.setTargetUserId(event.targetUserId());
        entity.setTargetRoleId(event.targetRoleId());
        entity.setTraceId(event.traceId());
        entity.setRequestContext(event.requestContext());
        entity.setOccurredAt(LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC));
        events.insert(entity);
    }

    private LoginIdentity toIdentity(SysUserEntity user) {
        return new LoginIdentity(user.getId(), user.getLoginName(), user.getDisplayName(),
            user.getPasswordHash(), Boolean.TRUE.equals(user.getEnabled()),
            user.getFailedLoginCount(), user.getLockedUntil() == null ? null
                : user.getLockedUntil().toInstant(ZoneOffset.UTC),
            Boolean.TRUE.equals(user.getMustChangePassword()), user.getVersion());
    }
}
