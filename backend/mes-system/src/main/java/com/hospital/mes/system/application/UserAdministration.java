package com.hospital.mes.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.password.TemporaryPasswordGenerator;
import com.hospital.mes.security.session.SessionStore;
import com.hospital.mes.system.infrastructure.SysUserEntity;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.hospital.mes.system.infrastructure.SysUserRoleMapper;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class UserAdministration {
    public record CreateUser(String loginName, String displayName) { }
    public record UserView(long id, String loginName, String displayName, boolean enabled,
                           long version, List<Long> roleIds) { }
    public record CreatedUser(UserView user, String temporaryPassword) { }
    public record TemporaryPassword(String value) { }

    private final SysUserMapper users;
    private final SysUserRoleMapper userRoles;
    private final PasswordService passwords;
    private final TemporaryPasswordGenerator temporaryPasswords;
    private final SessionStore sessions;
    private final AdminSecurityEventWriter events;
    private final AdminSafetyGuard safety;

    public UserAdministration(SysUserMapper users, SysUserRoleMapper userRoles,
                              PasswordService passwords, TemporaryPasswordGenerator temporaryPasswords,
                              SessionStore sessions, AdminSecurityEventWriter events,
                              AdminSafetyGuard safety) {
        this.users = users;
        this.userRoles = userRoles;
        this.passwords = passwords;
        this.temporaryPasswords = temporaryPasswords;
        this.sessions = sessions;
        this.events = events;
        this.safety = safety;
    }

    @Transactional(readOnly = true)
    public PageResult<UserView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page");
        long offset = Math.multiplyExact((long) page, size);
        List<UserView> items = users.selectList(new LambdaQueryWrapper<SysUserEntity>()
                .orderByAsc(SysUserEntity::getId).last("LIMIT " + size + " OFFSET " + offset))
            .stream().map(this::view).toList();
        return new PageResult<>(items, users.selectCount(null), page, size);
    }

    @Transactional(readOnly = true)
    public UserView get(long userId) {
        return view(required(userId));
    }

    @Transactional
    public CreatedUser create(CreateUser command, long actorId, String traceId) {
        if (command == null) throw new IllegalArgumentException("Invalid user");
        String login = validText(command.loginName(), 128);
        String normalized = LoginNameNormalizer.normalize(login);
        if (normalized.isBlank() || normalized.length() > 128) throw new IllegalArgumentException("Invalid login");
        String display = validText(command.displayName(), 128);
        String secret = temporaryPasswords.generate();
        SysUserEntity user = new SysUserEntity();
        user.setLoginName(login);
        user.setLoginNameNormalized(normalized);
        user.setDisplayName(display);
        user.setPasswordHash(passwords.hash(secret));
        user.setEnabled(true);
        user.setFailedLoginCount(0);
        user.setMustChangePassword(true);
        user.setVersion(0L);
        users.insert(user);
        events.append("USER_CREATED", actorId, user.getId(), null, null, traceId);
        return new CreatedUser(view(user), secret);
    }

    @Transactional
    public UserView profile(long userId, String displayName, long expectedVersion,
                            long actorId, String traceId) {
        required(userId);
        String display = validText(displayName, 128);
        if (users.updateDisplayName(userId, expectedVersion, display, actorId) != 1)
            throw conflict();
        events.append("USER_PROFILE_UPDATED", actorId, userId, null, null, traceId);
        return view(required(userId));
    }

    @Transactional
    public UserView setEnabled(long userId, boolean enabled, long expectedVersion,
                               long actorId, String traceId) {
        required(userId);
        Runnable change = () -> {
            if (users.updateEnabled(userId, expectedVersion, enabled, actorId) != 1) throw conflict();
        };
        if (!enabled && userRoles.systemAdminAssignmentCount(userId) > 0)
            safety.runPreservingEffectiveAdmin(change);
        else change.run();
        events.append(enabled ? "USER_ENABLED" : "USER_DISABLED", actorId, userId, null, null, traceId);
        return view(required(userId));
    }

    @Transactional
    public TemporaryPassword resetPassword(long userId, long actorId, String traceId) {
        SysUserEntity current = required(userId);
        String secret = temporaryPasswords.generate();
        String hash = passwords.hash(secret);
        sessions.revokeAllForUser(userId);
        if (users.resetAdminPassword(userId, current.getVersion(), hash, actorId) != 1) throw conflict();
        events.append("PASSWORD_RESET", actorId, userId, null, null, traceId);
        return new TemporaryPassword(secret);
    }

    private SysUserEntity required(long userId) {
        if (userId < 1) throw new NoSuchElementException("User not found");
        SysUserEntity user = users.selectById(userId);
        if (user == null) throw new NoSuchElementException("User not found");
        return user;
    }

    private UserView view(SysUserEntity user) {
        return new UserView(user.getId(), user.getLoginName(), user.getDisplayName(),
            Boolean.TRUE.equals(user.getEnabled()), user.getVersion(),
            userRoles.activeRoleIds(user.getId()));
    }

    private static String validText(String value, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength)
            throw new IllegalArgumentException("Invalid text");
        String stripped = value.strip();
        if (stripped.isEmpty() || stripped.length() > maxLength
            || stripped.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid text");
        return stripped;
    }

    private static ResourceConflictException conflict() {
        return new ResourceConflictException("ADMIN_CONFLICT", "Administrative change conflicts with current state");
    }
}
