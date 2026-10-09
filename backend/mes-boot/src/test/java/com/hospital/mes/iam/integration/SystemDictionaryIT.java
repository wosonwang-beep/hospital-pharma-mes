package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class SystemDictionaryIT {
 @Autowired JdbcTemplate jdbc;
 @Test void dictionaryMigrationHasRealTablesAndAdministratorPermissions() {
   var version=jdbc.queryForObject("SELECT success FROM flyway_schema_history WHERE version='039'",Integer.class);
   assertThat(version).isEqualTo(1);
   assertThat(jdbc.queryForObject("SELECT count(*) FROM sys_dict_type WHERE org_id=1",Long.class)).isGreaterThanOrEqualTo(3L);
   assertThat(jdbc.queryForList("SELECT permission_code FROM sys_permission WHERE permission_code LIKE 'iam:dict:%'",String.class))
     .containsExactlyInAnyOrder("iam:dict:view","iam:dict:create","iam:dict:update");
   assertThat(jdbc.queryForObject("SELECT count(*) FROM sys_menu WHERE menu_code='nav_dictionaries' AND route_path='/admin/dictionaries'",Long.class)).isEqualTo(1L);
   assertThat(jdbc.queryForObject("SELECT count(*) FROM sys_role_permission rp JOIN sys_role r ON r.id=rp.role_id JOIN sys_permission p ON p.id=rp.permission_id WHERE r.role_code='SYSTEM_ADMIN' AND p.permission_code='iam:dict:update'",Long.class)).isGreaterThan(0L);
 }
 @Test void dictionaryTypesAndItemsAreDatabaseBackedAndRollback() {
   String code="TEST_"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
   jdbc.update("INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,status,sort_no,created_by,updated_by) VALUES(1,?,?,'BUSINESS','TREE','ACTIVE',30,1,1)",code,"回滚验证");
   Long typeId=jdbc.queryForObject("SELECT id FROM sys_dict_type WHERE org_id=1 AND dict_code=?",Long.class,code);
   assertThat(typeId).isNotNull();
   jdbc.update("INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by) VALUES(1,?,'ROOT','根节点',10,'ACTIVE',1,1)",typeId);
   Long itemId=jdbc.queryForObject("SELECT id FROM sys_dict_item WHERE dict_type_id=? AND item_code='ROOT'",Long.class,typeId);
   jdbc.update("INSERT INTO sys_dict_item(org_id,dict_type_id,parent_id,item_code,item_label,sort_no,status,created_by,updated_by) VALUES(1,?,?,'CHILD','下级项目',20,'ACTIVE',1,1)",typeId,itemId);
   assertThat(jdbc.queryForObject("SELECT count(*) FROM sys_dict_item WHERE org_id=1 AND dict_type_id=?",Long.class,typeId)).isEqualTo(2L);
   assertThat(jdbc.queryForObject("SELECT item_label FROM sys_dict_item WHERE dict_type_id=? AND item_code='CHILD'",String.class,typeId)).isEqualTo("下级项目");
 }
}
