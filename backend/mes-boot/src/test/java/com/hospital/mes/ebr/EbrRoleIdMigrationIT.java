package com.hospital.mes.ebr;

import java.sql.*;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static com.hospital.mes.ebr.RoleIdIsolationSupport.*;

class EbrRoleIdMigrationIT {
 @Test void existingIsolationUpgradePreservesAllOldColumnsAndEnforcesRoleIntegrity()throws Exception {
  try(var c=connect()){
   verifyBackupAndSource(c);
   assertThat(scalar(c,"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1")).isEqualTo("57");
   // Flyway11.7.2 treats resolved58/59 as ABOVE_TARGET at target57, validating every
   // applied checksum without incorrectly classifying authorized pending work as corruption.
   var appliedChain=Flyway.configure().dataSource(url(),System.getenv("MES_ISOLATED_USER"),System.getenv("MES_ISOLATED_PASSWORD")).locations("classpath:db/migration").target("57").cleanDisabled(true).load();
   appliedChain.validate();assertThat(appliedChain.info().applied()).hasSize(57);
   assertThat(scalar(c,"SELECT COUNT(*) FROM qms_inspection_object_binding")).isEqualTo("0");
   assertThat(scalar(c,"SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME='qms_inspection_object_binding'")).isEqualTo("0");
   assertThat(rows(c,"SHOW OPEN TABLES FROM "+SCHEMA+" WHERE In_use>0")).isEmpty();
   assertThat(scalar(c,"SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE DB=DATABASE() AND ID<>CONNECTION_ID()")).isEqualTo("0");
   var oldColumns=columns(c);oldColumns.remove("qms_inspection_object_binding");oldColumns.remove("flyway_schema_history");var before=snapshot(c,oldColumns);var history=rows(c,"SELECT * FROM flyway_schema_history ORDER BY installed_rank");
   var flyway=Flyway.configure().dataSource(url(),System.getenv("MES_ISOLATED_USER"),System.getenv("MES_ISOLATED_PASSWORD")).locations("classpath:db/migration").target("59").cleanDisabled(true).load();
   // migrate retains normal validate-on-migrate; full validate is required after upgrade.
   var result=flyway.migrate();assertThat(result.migrationsExecuted).isEqualTo(2);flyway.validate();
   assertThat(snapshot(c,oldColumns)).isEqualTo(before);assertThat(rows(c,"SELECT * FROM flyway_schema_history WHERE installed_rank<=57 ORDER BY installed_rank")).isEqualTo(history);
   assertThat(scalar(c,"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1")).isEqualTo("59");
   assertThat(scalar(c,"SELECT checksum FROM flyway_schema_history WHERE CAST(version AS UNSIGNED)=58")).isEqualTo("946183528");
   assertThat(scalar(c,"SELECT checksum FROM flyway_schema_history WHERE CAST(version AS UNSIGNED)=59")).isEqualTo("-503587429");
   assertThat(scalar(c,"SELECT COUNT(*) FROM ebr_template_version WHERE definition_schema_version<>1")).isEqualTo("0");
   for(String table:List.of("ebr_signature_rule","ebr_review_rule"))assertThat(scalar(c,"SELECT COUNT(*) FROM "+table+" WHERE required_role_id IS NOT NULL OR required_role_name_snapshot IS NOT NULL")).isEqualTo("0");
   assertThat(scalar(c,"SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME IN ('fk_ebr_signature_required_role','fk_ebr_review_required_role') AND DELETE_RULE='RESTRICT' AND UPDATE_RULE='RESTRICT'")).isEqualTo("2");
   assertThat(scalar(c,"SELECT COUNT(DISTINCT TABLE_NAME,INDEX_NAME) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND INDEX_NAME IN ('ix_ebr_signature_required_role','ix_ebr_review_required_role')")).isEqualTo("2");
   c.setAutoCommit(false);try{
    for(String table:List.of("ebr_signature_rule","ebr_review_rule")){
     fails(c,"UPDATE "+table+" SET required_role_id=999999999,required_role_name_snapshot='missing' WHERE id=(SELECT id FROM (SELECT MIN(id) id FROM "+table+") x)");
     fails(c,"UPDATE "+table+" SET required_role_id=290,required_role_name_snapshot=NULL WHERE id=(SELECT id FROM (SELECT MIN(id) id FROM "+table+") x)");
    }
    fails(c,"UPDATE ebr_template_version SET definition_schema_version=3 WHERE id=291");
   }finally{c.rollback();c.setAutoCommit(true);}
   assertThat(snapshot(c,oldColumns)).isEqualTo(before);
   // LinkedHashMap retains report order and permits null values if later evidence is unavailable.
   var evidence=new LinkedHashMap<String,Object>();evidence.put("schema",SCHEMA);evidence.put("beforeVersion",57);evidence.put("afterVersion",59);evidence.put("appliedVersions",List.of(58,59));evidence.put("checksums",Map.of("58",946183528,"59",-503587429));evidence.put("unchangedOldColumnTables",oldColumns.size());evidence.put("allOldRowsEqual",true);evidence.put("noBackfill",true);evidence.put("negativeIntegrityChecks",5);evidence.put("negativeChecksRolledBack",true);evidence.put("ddlPersisted",true);evidence.put("DEVWrites",false);evidence.put("processVisibility","Current-account only; PROCESS privilege unavailable");report("isolated-migration-result.json",evidence);
  }
 }
 private static void fails(Connection c,String sql)throws SQLException {var point=c.setSavepoint();try{assertThatThrownBy(()->{try(var s=c.createStatement()){s.executeUpdate(sql);}}).isInstanceOf(SQLException.class);}finally{c.rollback(point);c.releaseSavepoint(point);}}
}
