package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.system.application.IamContractService;
import com.hospital.mes.system.application.IamContractService.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Uses the configured local database; every test transaction rolls back. */
@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class DatabaseMenuAuthorizationIT {
    @Autowired IamContractService iam;
    @Autowired JdbcTemplate jdbc;
    private CurrentPlatformContext context() {
        long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class);
        return new CurrentPlatformContext(1,actor,Set.of("SYSTEM_ADMIN"),Set.of("iam:menu:update","iam:role:update"),"menu-rollback-test",UUID.randomUUID().toString());
    }
    private String key(){return UUID.randomUUID().toString();}
    private IamMenuResponse directory(String parent,CurrentPlatformContext c){return iam.createMenu(new CreateMenuRequest("TEST_"+key(),"测试目录",null,parent,100,"ACTIVE","回滚测试",null,""),c,key());}
    private IamMenuResponse page(String parent,CurrentPlatformContext c){return iam.createMenu(new CreateMenuRequest("TEST_"+key(),"测试菜单","/admin/users",parent,100,"ACTIVE","回滚测试","iam:user:view",""),c,key());}
    @Test void functionBindingsVersionAndAuditAreStoredTogether(){
        var c=context();var menu=page(null,c);long id=Long.parseLong(menu.id());
        var bound=iam.assignMenuPermissions(id,menu.version(),new AssignMenuPermissionsRequest(List.of("iam:user:view","iam:user:create"),"回滚测试"),c,key());
        assertThat(bound.version()).isEqualTo(menu.version()+1);
        assertThat(bound.permissionCodes()).containsExactlyInAnyOrder("iam:user:view","iam:user:create");
        assertThatThrownBy(()->iam.assignMenuPermissions(id,menu.version(),new AssignMenuPermissionsRequest(List.of(),"回滚测试"),c,key())).isInstanceOf(RuntimeException.class);
        assertThat(iam.getMenu(id,1).permissionCodes()).containsExactlyInAnyOrderElementsOf(bound.permissionCodes());
    }
    @Test void sameViewPermissionDoesNotForceVisibilityForBothMenus(){
        var c=context();var first=page(null,c);var second=page(null,c);
        var role=iam.createRole(new CreateRoleRequest("TEST_"+key().replace("-","").toUpperCase(),"测试岗位","回滚测试"),c,key());
        var assigned=iam.assignRolePermissions(Long.parseLong(role.id()),role.version(),new AssignRolePermissionsRequest(List.of("iam:user:view"),"回滚测试",List.of(first.menuCode())),c,key());
        assertThat(assigned.menuCodes()).containsExactly(first.menuCode()).doesNotContain(second.menuCode());
        assertThat(assigned.permissionCodes()).containsExactly("iam:user:view");
    }
    @Test void ancestorCycleAndMissingMenuViewPermissionAreRejected(){
        var c=context();var root=directory(null,c);var child=directory(root.id(),c);
        assertThatThrownBy(()->iam.updateMenu(Long.parseLong(root.id()),root.version(),new UpdateMenuRequest("测试目录",null,child.id(),100,"ACTIVE","回滚测试",null,""),c,key())).isInstanceOf(IllegalArgumentException.class);
        var menu=page(null,c);var role=iam.createRole(new CreateRoleRequest("TEST_"+key().replace("-","").toUpperCase(),"测试岗位","回滚测试"),c,key());
        assertThatThrownBy(()->iam.assignRolePermissions(Long.parseLong(role.id()),role.version(),new AssignRolePermissionsRequest(List.of(),"回滚测试",List.of(menu.menuCode())),c,key())).isInstanceOf(IllegalArgumentException.class);
    }
}
