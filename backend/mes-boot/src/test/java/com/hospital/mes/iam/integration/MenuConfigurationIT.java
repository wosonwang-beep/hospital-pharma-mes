package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.system.application.IamContractService;
import com.hospital.mes.system.application.IamContractService.*;
import com.hospital.mes.system.application.IamMutationExecutor;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class MenuConfigurationIT {
    @Autowired IamContractService iam;
    @Autowired IamMutationExecutor mutations;
    @Autowired JdbcTemplate jdbc;
    String key(){return UUID.randomUUID().toString();}
    CurrentPlatformContext context(Set<String> rights){
        long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class);
        return new CurrentPlatformContext(1,actor,Set.of("SYSTEM_ADMIN"),rights,"menu-configuration-test",key());
    }
    CurrentPlatformContext context(){return context(Set.of("iam:menu:create","iam:menu:update","iam:permission:view","iam:permission:create","iam:permission:update"));}
    CreateMenuRequest menu(String name){return new CreateMenuRequest("TEST_"+key(),name,"/admin/menus",null,100,"ACTIVE","rollback test","iam:menu:view",null);}
    String code(){return "iam:config-"+key().replace("-","")+":export";}
    @Test void descriptionAndOldRequestsRemainCompatible(){
        var c=context();var p=iam.createPermission(new CreatePermissionRequest(code(),"测试权限","ACTION",null,"ACTIVE","rollback test","原说明"),c,key());
        assertThat(p.description()).isEqualTo("原说明");
        var updated=iam.updatePermission(Long.parseLong(p.id()),p.version(),new UpdatePermissionRequest("更新名称","ACTION",null,"ACTIVE","rollback test"),c,key());
        assertThat(updated.description()).isEqualTo("原说明");
        var cleared=iam.updatePermission(Long.parseLong(p.id()),updated.version(),new UpdatePermissionRequest("更新名称","ACTION",null,"ACTIVE","rollback test",""),c,key());
        assertThat(cleared.description()).isEmpty();
    }
    @Test void aggregateAndUnlinkPreserveRoleGrants(){
        var c=context();String code=code();var request=new MenuConfigurationRequest(menu("测试配置"),List.of("iam:menu:view",code),List.of(new PermissionChange(null,null,code,"测试导出","真实说明","ACTIVE")));
        var created=iam.saveMenuConfiguration(null,null,request,c,key());
        assertThat(created.permissionCodes()).containsExactlyInAnyOrder("iam:menu:view",code);
        var p=iam.listPermissions(0,100,code,null).items().getFirst();assertThat(p.description()).isEqualTo("真实说明");
        long roleId=Long.parseLong(iam.createRole(new CreateRoleRequest("TEST_"+key().replace("-","").toUpperCase(),"回滚测试角色","rollback test"),c,key()).id());
        jdbc.update("INSERT INTO sys_role_permission(role_id,permission_id) VALUES(?,?)",roleId,Long.parseLong(p.id()));
        var m=request.menu();var saved=iam.saveMenuConfiguration(Long.parseLong(created.id()),created.version(),new MenuConfigurationRequest(new CreateMenuRequest(created.menuCode(),m.menuName(),m.routePath(),m.parentId(),m.sortNo(),m.status(),m.reason(),m.permissionCode(),m.requiredPermissions()),List.of("iam:menu:view"),List.of()),c,key());
        assertThat(saved.permissionCodes()).doesNotContain(code);
        assertThat(iam.getPermission(Long.parseLong(p.id())).permissionCode()).isEqualTo(code);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission WHERE role_id=? AND permission_id=?",Integer.class,roleId,Long.parseLong(p.id()))).isEqualTo(1);
    }
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void failureRollsBackNewPermissionMenuAndAudit(){
        var c=context();String code=code();var m=menu("失败不应保存");
        assertThatThrownBy(()->iam.saveMenuConfiguration(null,null,new MenuConfigurationRequest(m,List.of("iam:menu:view",code,"iam:nonexistent-config:view"),List.of(new PermissionChange(null,null,code,"不应保存","回滚说明","ACTIVE"))),c,key())).isInstanceOf(NoSuchElementException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_permission WHERE permission_code=?",Integer.class,code)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE menu_code=?",Integer.class,m.menuCode())).isZero();
    }
    @Test void permissionChecksAndStaleMenuRejectWithoutEscalation(){
        var denied=context(Set.of("iam:menu:create","iam:permission:view"));String code=code();
        assertThatThrownBy(()->iam.saveMenuConfiguration(null,null,new MenuConfigurationRequest(menu("不能新增权限"),List.of("iam:menu:view",code),List.of(new PermissionChange(null,null,code,"不可创建",null,"ACTIVE"))),denied,key())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
        var c=context();var created=iam.saveMenuConfiguration(null,null,new MenuConfigurationRequest(menu("测试版本"),List.of("iam:menu:view"),List.of()),c,key());
        assertThatThrownBy(()->iam.saveMenuConfiguration(Long.parseLong(created.id()),created.version()+1,new MenuConfigurationRequest(new CreateMenuRequest(created.menuCode(),"不应更新",created.routePath(),null,100,"ACTIVE","rollback test","iam:menu:view",null),List.of("iam:menu:view"),List.of()),c,key())).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
        assertThat(iam.getMenu(Long.parseLong(created.id()),1).menuName()).isEqualTo("测试版本");
        var other=new CurrentPlatformContext(999999,c.actorId(),c.roleCodes(),c.permissionCodes(),c.sessionId(),key());
        assertThatThrownBy(()->iam.saveMenuConfiguration(Long.parseLong(created.id()),created.version(),new MenuConfigurationRequest(menu("跨组织"),List.of("iam:menu:view"),List.of()),other,key())).isInstanceOf(NoSuchElementException.class);
    }
    @Test void idempotencyReplaysWithoutDuplicateDefinitions(){
        var c=context();String code=code(),key=key();var request=new MenuConfigurationRequest(menu("幂等测试"),List.of("iam:menu:view",code),List.of(new PermissionChange(null,null,code,"导出测试",null,"ACTIVE")));
        var first=mutations.execute(c,"createMenuConfiguration",key,request,"MENU",()->iam.saveMenuConfiguration(null,null,request,c,key),IamMenuResponse::id);
        var replay=mutations.execute(c,"createMenuConfiguration",key,request,"MENU",()->{throw new AssertionError("replay cannot execute writes");},IamMenuResponse::id);
        assertThat(replay).isEqualTo(first);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_permission WHERE permission_code=?",Integer.class,code)).isEqualTo(1);
    }
}
