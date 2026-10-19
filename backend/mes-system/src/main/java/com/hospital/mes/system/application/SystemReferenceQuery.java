package com.hospital.mes.system.application;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import com.hospital.mes.security.context.PlatformOrganizationResolver;
import com.hospital.mes.system.infrastructure.*;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
/** Read-only references; IAM roles retain their existing global scope. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SystemReferenceQuery implements CurrentSigningAuthority {
    private final SysRoleMapper roles; private final SysUserMapper users;
    private final SysRolePermissionMapper memberships; private final PlatformOrganizationResolver organizations;
    public SystemReferenceQuery(SysRoleMapper roles,SysUserMapper users,SysRolePermissionMapper memberships,PlatformOrganizationResolver organizations){this.roles=roles;this.users=users;this.memberships=memberships;this.organizations=organizations;}
    public record RoleOption(String id,String code,String name,boolean enabled){}
    public record RoleOptions(List<RoleOption> items,long total,int page,int size){}
    public void requireRole(String code){enabledRoleByCode(code);}
    public RoleOption enabledRoleByCode(String code){
        if(code==null||code.isBlank())throw new NoSuchElementException("Enabled role not found");
        var found=roles.selectList(new QueryWrapper<SysRoleEntity>().eq("role_code",code).eq("enabled",true));
        if(found.size()!=1)throw new NoSuchElementException("Unique enabled role not found");return option(found.getFirst());
    }
    public RoleOption requireRoleId(String id){
        final long value;try{if(id==null||!id.matches("[1-9][0-9]*"))throw new IllegalArgumentException();value=Long.parseLong(id);}catch(RuntimeException ex){throw new IllegalArgumentException("Positive role ID required");}
        var role=roles.selectById(value);if(role==null||!Boolean.TRUE.equals(role.getEnabled()))throw new NoSuchElementException("Enabled role not found");return option(role);
    }
    public RoleOptions roleOptions(int page,int size,String keyword){
        if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid role option page");
        var query=new QueryWrapper<SysRoleEntity>().eq("enabled",true);
        if(keyword!=null&&!keyword.isBlank()){String term=keyword.strip();if(term.length()>128)throw new IllegalArgumentException("Role keyword too long");query.and(q->q.like("role_code",term).or().like("display_name",term));}
        long count=roles.selectCount(query);query.orderByAsc("id").last("LIMIT "+size+" OFFSET "+Math.multiplyExact((long)page,size));
        return new RoleOptions(roles.selectList(query).stream().map(SystemReferenceQuery::option).toList(),count,page,size);
    }
    private static RoleOption option(SysRoleEntity role){return new RoleOption(role.getId().toString(),role.getRoleCode(),role.getDisplayName(),Boolean.TRUE.equals(role.getEnabled()));}
    @Override @Transactional(propagation=Propagation.MANDATORY,readOnly=true)
    public CurrentPlatformContext requireCurrent(CurrentPlatformContext actor,Set<String> permissions){
        if(actor.organizationId()!=organizations.organizationId())throw new PermissionException("PERMISSION_DENIED","Current organization binding required");
        var user=users.selectById(actor.actorId());if(user==null||!Boolean.TRUE.equals(user.getEnabled()))throw new PermissionException("PERMISSION_DENIED","Current enabled user required");
        Set<String> currentPermissions=Set.copyOf(memberships.activePermissionCodes(actor.actorId()));
        if(!currentPermissions.containsAll(permissions))throw new PermissionException("PERMISSION_DENIED","Current signing/review permission required");
        Set<String> currentIds=memberships.activeRoleIds(actor.actorId()).stream().map(String::valueOf).collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new CurrentPlatformContext(actor.organizationId(),actor.actorId(),Set.copyOf(memberships.activeRoleCodes(actor.actorId())),currentPermissions,actor.sessionId(),actor.requestId(),currentIds,2);
    }
}
