package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.system.api.SystemDepartmentController;
import com.hospital.mes.system.api.SystemDepartmentController.DepartmentCommand;
import com.hospital.mes.system.api.SystemDepartmentController.DepartmentAssignment;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class SystemDepartmentIT {
 @Autowired SystemDepartmentController departments;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean CurrentPlatformContextResolver contexts;
 private final String session="department-rollback-test";
 private String key(){return UUID.randomUUID().toString();}
 private String code(){return "DEP_"+key().replace("-","").substring(0,12).toUpperCase();}
 private String organization;
 private String admin;
 private DepartmentCommand command(String code,String name,String parent){
  return new DepartmentCommand(code,name,organization,parent,null,null,"部门集成测试事务回滚",10,"ACTIVE");
 }
 @BeforeEach void init(){
  admin=String.valueOf(jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class));
  organization=String.valueOf(jdbc.queryForObject(
   "SELECT id FROM md_organization WHERE org_id=1 AND status='ACTIVE' ORDER BY id LIMIT 1",Long.class));
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,Long.parseLong(admin),Set.of("SYSTEM_ADMIN"),
      Set.of("iam:department:view","iam:department:create","iam:department:update","iam:user:update","iam:user:view"),session,key()));
  var grants=List.of("iam:department:view","iam:department:create","iam:department:update","iam:user:update","iam:user:view")
      .stream().map(SimpleGrantedAuthority::new).toList();
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("department-it","n/a",grants));
 }
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void migrationAndDepartmentPermissionExist(){
  assertThat(jdbc.queryForObject("SELECT success FROM flyway_schema_history WHERE version='040'",Integer.class)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE org_id=1 AND menu_code='nav_departments'",Long.class)).isEqualTo(1);
  assertThat(jdbc.queryForList("SELECT permission_code FROM sys_permission WHERE permission_code LIKE 'iam:department:%'",String.class))
      .containsExactlyInAnyOrder("iam:department:view","iam:department:create","iam:department:update");
 }
 @Test void createTreeUpdateAndRejectCycle(){
  String firstCode=code();
  var root=departments.create(key(),command(firstCode,"研发中心",null)).data();
  String rootId=root.get("id").asText();
  var child=departments.create(key(),command(code(),"分析实验室",rootId)).data();
  String childId=child.get("id").asText();
  assertThat(departments.get(childId).data().parentId()).isEqualTo(rootId);
  assertThat(departments.tree().data()).anySatisfy(item->{
   if(item.id().equals(childId))assertThat(item.parentId()).isEqualTo(rootId);
  });
  assertThatThrownBy(()->departments.update(rootId,"\"0\"",key(),command(firstCode,"研发中心",childId)))
   .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cycle");
  var changed=departments.update(childId,"\"0\"",key(),command(child.get("code").asText(),"质量分析",rootId)).data();
  assertThat(changed.get("name").asText()).isEqualTo("质量分析");
  assertThatThrownBy(()->departments.update(childId,"\"0\"",key(),command(child.get("code").asText(),"旧版本覆盖",rootId)))
   .isInstanceOf(RuntimeException.class);
 }
 @Test void userAssignmentHasRealJoinAndMayBeCleared(){
  String departmentId=departments.create(key(),command(code(),"归属测试",null)).data().get("id").asText();
  var assigned=departments.assign(admin,key(),new DepartmentAssignment(departmentId)).data();
  assertThat(assigned.get("departmentId").asText()).isEqualTo(departmentId);
  assertThat(departments.getAssignment(admin).data().departmentName()).isEqualTo("归属测试");
  var disabled=new DepartmentCommand(departments.get(departmentId).data().code(),"归属测试",organization,
      null,null,null,null,10,"INACTIVE");
  assertThatThrownBy(()->departments.update(departmentId,"\"0\"",key(),disabled))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("members");
  var cleared=departments.assign(admin,key(),new DepartmentAssignment(null)).data();
  assertThat(cleared.get("departmentId").isNull()).isTrue();
  assertThat(departments.getAssignment(admin).data().departmentId()).isNull();
 }
}
