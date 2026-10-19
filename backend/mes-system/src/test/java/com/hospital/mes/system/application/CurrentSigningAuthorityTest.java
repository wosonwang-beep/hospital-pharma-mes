package com.hospital.mes.system.application;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import com.hospital.mes.security.context.PlatformOrganizationResolver;
import com.hospital.mes.system.infrastructure.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CurrentSigningAuthorityTest {
    SysUserMapper users=mock(SysUserMapper.class);SysRolePermissionMapper memberships=mock(SysRolePermissionMapper.class);SysRoleMapper roles=mock(SysRoleMapper.class);PlatformOrganizationResolver org=mock(PlatformOrganizationResolver.class);SystemReferenceQuery query;SysUserEntity user;
    CurrentPlatformContext cached=new CurrentPlatformContext(1,11,Set.of("OLD"),Set.of("ebr:sign","ebr:review:verify"),"s","r",Set.of("290"),2);
    @BeforeEach void setup(){query=new SystemReferenceQuery(roles,users,memberships,org);when(org.organizationId()).thenReturn(1L);user=new SysUserEntity();user.setEnabled(true);when(users.selectById(11L)).thenReturn(user);when(memberships.activeRoleIds(11)).thenReturn(List.of(290L,291L));when(memberships.activeRoleCodes(11)).thenReturn(List.of("NEW","QA"));when(memberships.activePermissionCodes(11)).thenReturn(List.of("ebr:sign","ebr:review:verify"));}
    @Test void currentEnabledRolesAreScopedToExactUser(){var fresh=query.requireCurrent(cached,Set.of("ebr:sign"));assertEquals(Set.of("290","291"),fresh.roleIds());assertEquals(11,fresh.actorId());verify(memberships).activeRoleIds(11);}
    @Test void revocationCommittedBeforeCommandRejectsCachedPermission(){when(memberships.activePermissionCodes(11)).thenReturn(List.of("ebr:review:verify"));assertThrows(PermissionException.class,()->query.requireCurrent(cached,Set.of("ebr:sign")));}
    @Test void disabledUserIsRejected(){user.setEnabled(false);assertThrows(PermissionException.class,()->query.requireCurrent(cached,Set.of("ebr:sign")));verifyNoInteractions(memberships);}
    @Test void wrongOrganizationIsRejected(){when(org.organizationId()).thenReturn(2L);assertThrows(PermissionException.class,()->query.requireCurrent(cached,Set.of("ebr:sign")));verifyNoInteractions(users,memberships);}
    @Test void requiredConcreteReviewPermissionIsChecked(){when(memberships.activePermissionCodes(11)).thenReturn(List.of("ebr:sign"));assertThrows(PermissionException.class,()->query.requireCurrent(cached,Set.of("ebr:sign","ebr:review:verify")));}
    @Test void readonlyOptionsHaveNoMemberOrPermissionPayload(){var fields=Arrays.stream(SystemReferenceQuery.RoleOption.class.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).toList();assertEquals(List.of("id","code","name","enabled"),fields);assertThrows(IllegalArgumentException.class,()->query.roleOptions(0,101,null));}
}
