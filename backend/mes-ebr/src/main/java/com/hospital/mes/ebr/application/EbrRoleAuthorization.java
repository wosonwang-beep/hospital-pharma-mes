package com.hospital.mes.ebr.application;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrRoleAuthorization {
    private final CurrentSigningAuthority authority;
    public EbrRoleAuthorization(CurrentSigningAuthority authority){this.authority=authority;}
    public CurrentPlatformContext current(CurrentPlatformContext actor,String... permissions){
        if(actor.identityFormatVersion()!=2)throw new PermissionException("RELOGIN_REQUIRED","Sign in again for role ID signing");
        return authority.requireCurrent(actor,Set.of(permissions));
    }
    public static boolean matches(String schema,String roleId,String legacyCode,CurrentPlatformContext current){
        if("2.0".equals(schema))return roleId!=null&&roleId.matches("[1-9][0-9]*")&&current.roleIds().contains(roleId);
        return legacyCode!=null&&current.roleCodes().contains(legacyCode);
    }
}
