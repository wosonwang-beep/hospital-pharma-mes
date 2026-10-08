package com.hospital.mes.printing;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;import org.flywaydb.core.Flyway;import java.sql.*;import static org.junit.jupiter.api.Assertions.*;
/** Runs only against an explicitly approved, existing empty disposable CI catalog. Never reads the root .env or creates a database. */
@EnabledIfEnvironmentVariable(named="MES_PRINT_ISOLATED_DB_APPROVED",matches="true")
class ControlledPrintingMigrationIT {
 @Test void fullHistoricalChainThenPrintingImmutabilityAndOrganizationConstraints()throws Exception {
  String url=System.getenv("MES_PRINT_ISOLATED_JDBC_URL"),user=System.getenv("MES_PRINT_ISOLATED_DB_USER"),password=System.getenv("MES_PRINT_ISOLATED_DB_PASSWORD");
  assertNotNull(url,"Explicit isolated target required; shared DEV is forbidden");assertTrue(url.matches("jdbc:mariadb://[^/]+/hospital_pharma_mes_printing_ci_[a-z0-9]+(?:\\?.*)?"),"Only approved disposable printing CI catalogs are accepted");
  try(var connection=DriverManager.getConnection(url,user,password);var statement=connection.createStatement()){
   assertTrue(connection.getCatalog().matches("hospital_pharma_mes_printing_ci_[a-z0-9]+"));
   try(var tables=statement.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()")){tables.next();assertEquals(0,tables.getInt(1),"Target must be empty; refusing existing business catalog");}
  }
  var result=Flyway.configure().dataSource(url,user,password).locations("classpath:db/migration").cleanDisabled(true).load().migrate();assertTrue(result.migrationsExecuted>=33);assertTrue(result.success);
  try(var connection=DriverManager.getConnection(url,user,password);var statement=connection.createStatement()){
   try(var history=statement.executeQuery("SELECT success FROM flyway_schema_history WHERE version='033' OR version='33'")){assertTrue(history.next());assertTrue(history.getBoolean(1));}
   try(var permissions=statement.executeQuery("SELECT COUNT(*) FROM sys_permission WHERE permission_code IN ('print:template:view','print:template:manage','print:template:publish','print:document:generate')")){permissions.next();assertEquals(4,permissions.getInt(1));}
   statement.executeUpdate("INSERT INTO mes_print_template_version(id,org_id,template_code,template_name,template_revision,business_type,status,content_hash,docx,created_by,created_at,updated_by,updated_at) VALUES(990001,990001,'CI-SYNTHETIC','synthetic-only',1,'INSPECTION_REPORT','DRAFT',REPEAT('a',64),X'01',990001,NOW(),990001,NOW())");
   assertThrows(SQLException.class,()->statement.executeUpdate("UPDATE mes_print_template_version SET docx=X'02' WHERE id=990001"));
   statement.executeUpdate("UPDATE mes_print_template_version SET status='VALIDATED',preview_pdf=X'01',preview_hash=REPEAT('b',64),version_no=1 WHERE id=990001");statement.executeUpdate("UPDATE mes_print_template_version SET status='PUBLISHED',published_at=NOW(),version_no=2 WHERE id=990001");
   assertThrows(SQLException.class,()->statement.executeUpdate("UPDATE mes_print_template_version SET preview_pdf=X'02' WHERE id=990001"));assertThrows(SQLException.class,()->statement.executeUpdate("DELETE FROM mes_print_template_version WHERE id=990001"));
   assertThrows(SQLException.class,()->statement.executeUpdate("INSERT INTO mes_print_binding(org_id,business_type,template_version_id,enabled,created_by,created_at,updated_by,updated_at) VALUES(990002,'INSPECTION_REPORT',990001,TRUE,990001,NOW(),990001,NOW())"));
   statement.executeUpdate("INSERT INTO mes_print_artifact(id,org_id,business_type,business_id,business_version,report_no,template_version_id,template_revision,formal,snapshot_json,snapshot_hash,pdf_hash,docx,pdf,formal_key,created_by,created_at,updated_by,updated_at) VALUES(990001,990001,'INSPECTION_REPORT','CI-SYNTHETIC','1','DEMO',990001,1,TRUE,'{}',REPEAT('c',64),REPEAT('d',64),X'01',X'01',REPEAT('e',64),990001,NOW(),990001,NOW())");
   assertThrows(SQLException.class,()->statement.executeUpdate("UPDATE mes_print_artifact SET business_version='2' WHERE id=990001"));assertThrows(SQLException.class,()->statement.executeUpdate("DELETE FROM mes_print_artifact WHERE id=990001"));
   statement.executeUpdate("UPDATE mes_print_template_version SET status='INACTIVE' WHERE id=990001");assertThrows(SQLException.class,()->statement.executeUpdate("UPDATE mes_print_template_version SET status='PUBLISHED' WHERE id=990001"));
  }
 }
}
