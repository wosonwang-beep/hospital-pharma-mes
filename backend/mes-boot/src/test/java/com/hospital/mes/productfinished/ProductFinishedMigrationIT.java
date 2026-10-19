package com.hospital.mes.productfinished;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Explicitly selected isolated-schema gate; never loads a DEV application profile or @Commit seed. */
class ProductFinishedMigrationIT {
 @Test void upgradePreservesHistoryAndEnforcesDirectProductAndNewInstructionIdentity() throws Exception {
  String url=System.getenv("MES_ISOLATED_URL"),user=System.getenv("MES_ISOLATED_USER"),password=System.getenv("MES_ISOLATED_PASSWORD");
  assertThat(url).matches("jdbc:mariadb://127\\.0\\.0\\.1:3306/test_mes_product_finished_20261011_0640");
  try(var db=DriverManager.getConnection(url,user,password)){
   assertThat(scalar(db,"SELECT DATABASE()")).isEqualTo("test_mes_product_finished_20261011_0640");assertThat(scalar(db,"SELECT @@foreign_key_checks")).isEqualTo("1");
   var protectedRows=rows(db,"SELECT * FROM md_product ORDER BY id");var materials=rows(db,"SELECT * FROM md_material ORDER BY id");var signatures=rows(db,"SELECT * FROM gxp_signature ORDER BY id");
   var flyway=Flyway.configure().dataSource(url,user,password).locations("classpath:db/migration").cleanDisabled(true).load();assertThat(flyway.validateWithResult().validationSuccessful).isTrue();flyway.migrate();flyway.validate();
   assertThat(scalar(db,"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1")).isEqualTo("57");
   assertThat(rows(db,"SELECT * FROM md_product ORDER BY id")).isEqualTo(protectedRows);assertThat(rows(db,"SELECT * FROM md_material ORDER BY id")).isEqualTo(materials);assertThat(rows(db,"SELECT * FROM gxp_signature ORDER BY id")).isEqualTo(signatures);
   assertThat(scalar(db,"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='wms_finished_lot'")).isEqualTo("0");
   assertThat(scalar(db,"SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema=DATABASE() AND constraint_name='fk_finished_ledger_batch_product_unit'")).isEqualTo("4");
   assertThat(scalar(db,"SELECT COUNT(*) FROM md_material WHERE id IN (17,18)")).isEqualTo("0");
   long org=Long.parseLong(scalar(db,"SELECT org_id FROM md_product ORDER BY id LIMIT 1")),product=Long.parseLong(scalar(db,"SELECT id FROM md_product ORDER BY id LIMIT 1")),unit=Long.parseLong(scalar(db,"SELECT base_unit_id FROM md_product ORDER BY id LIMIT 1"));
   long actor=Long.parseLong(scalar(db,"SELECT created_by FROM md_product WHERE org_id="+org+" ORDER BY id LIMIT 1"));String marker="PF_ISOLATED_"+UUID.randomUUID().toString().replace("-","");
   db.setAutoCommit(false);
   try{
    long order=insert(db,"INSERT INTO prd_production_order(org_id,created_by,updated_by,order_no,product_id,planned_qty,unit_id,status) VALUES(?,?,?, ?,?,10,?,'DRAFT')",org,actor,actor,marker,product,unit);
    long batch=insert(db,"INSERT INTO prd_main_batch(org_id,created_by,updated_by,batch_no,production_order_id,product_id,planned_qty,unit_id,status,production_schema_version) VALUES(?,?,?,?,?,?,10,?,'DRAFT',2)",org,actor,actor,marker,order,product,unit);
    fails(db,"INSERT INTO prd_main_batch(org_id,created_by,updated_by,batch_no,production_order_id,product_id,planned_qty,unit_id,status,production_schema_version) VALUES(?,?,?,?,?,?,10,?,'DRAFT',2)",org,actor,actor,marker+"_DUP",order,product,unit);
    long legacyOrder=insert(db,"INSERT INTO prd_production_order(org_id,created_by,updated_by,order_no,product_id,planned_qty,unit_id,status) VALUES(?,?,?,?,?,20,?,'DRAFT')",org,actor,actor,marker+"_H",product,unit);
    for(int i=0;i<2;i++)insert(db,"INSERT INTO prd_main_batch(org_id,created_by,updated_by,batch_no,production_order_id,product_id,planned_qty,unit_id,status) VALUES(?,?,?,?,?,?,10,?,'DRAFT')",org,actor,actor,marker+"_H"+i,legacyOrder,product,unit);
    long material=Long.parseLong(scalar(db,"SELECT id FROM md_material WHERE org_id="+org+" ORDER BY id LIMIT 1"));
    // Preserve old MATERIAL and add equivalent PRODUCT same-organization rejection.
    fails(db,"INSERT INTO qc_specification(org_id,created_by,updated_by,specification_code,specification_name,subject_type,material_id) VALUES(?,?,?,?,?,'MATERIAL',?)",org+100000,actor,actor,marker+"_ORG_M","Wrong organization",material);
    fails(db,"INSERT INTO qc_specification(org_id,created_by,updated_by,specification_code,specification_name,subject_type,product_id) VALUES(?,?,?,?,?,'PRODUCT',?)",org+100000,actor,actor,marker+"_ORG_P","Wrong organization",product);
    insert(db,"INSERT INTO qc_specification(org_id,created_by,updated_by,specification_code,specification_name,subject_type,product_id) VALUES(?,?,?,?,?,'PRODUCT',?)",org,actor,actor,marker,"Product standard",product);
    fails(db,"INSERT INTO qc_specification(org_id,created_by,updated_by,specification_code,specification_name,subject_type,material_id,product_id) VALUES(?,?,?,?,?,'PRODUCT',?,?)",org,actor,actor,marker+"_BAD","Wrong subject",material,product);
    // Stock cannot be produced before OUTPUT freezes the batch's base unit.
    long location=Long.parseLong(scalar(db,"SELECT id FROM wms_location WHERE org_id="+org+" ORDER BY id LIMIT 1"));
    fails(db,"INSERT INTO wms_finished_inventory_ledger(org_id,created_by,updated_by,created_at,updated_at,main_batch_id,product_id,location_id,unit_id,event_type,delta_qty,inbound_request_id,idempotency_key,occurred_at) VALUES(?,?,?,NOW(3),NOW(3),?,?,?,?,'FINISHED_RECEIPT',1,?, ?,NOW(3))",org,actor,actor,batch,product,location,unit,999999999L,marker);
   }finally{db.rollback();db.setAutoCommit(true);}
   assertThat(rows(db,"SELECT * FROM md_product ORDER BY id")).isEqualTo(protectedRows);assertThat(rows(db,"SELECT * FROM md_material ORDER BY id")).isEqualTo(materials);assertThat(rows(db,"SELECT * FROM gxp_signature ORDER BY id")).isEqualTo(signatures);
   assertThat(scalar(db,"SELECT COUNT(*) FROM prd_main_batch WHERE batch_no LIKE '"+marker+"%'")).isEqualTo("0");assertThat(scalar(db,"SELECT @@foreign_key_checks")).isEqualTo("1");
  }
 }
 private static String scalar(Connection db,String sql)throws SQLException{try(var s=db.createStatement();var r=s.executeQuery(sql)){assertThat(r.next()).isTrue();return r.getString(1);}}
 private static List<List<String>> rows(Connection db,String sql)throws SQLException{var out=new ArrayList<List<String>>();try(var s=db.createStatement();var r=s.executeQuery(sql)){int columns=r.getMetaData().getColumnCount();while(r.next()){var row=new ArrayList<String>();for(int i=1;i<=columns;i++)row.add(r.getString(i));out.add(row);}}return out;}
 private static long insert(Connection db,String sql,Object...values)throws SQLException{try(var s=db.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();try(var r=s.getGeneratedKeys()){assertThat(r.next()).isTrue();return r.getLong(1);}}}
 private static void fails(Connection db,String sql,Object...values)throws SQLException{var point=db.setSavepoint();try{assertThatThrownBy(()->insert(db,sql,values)).isInstanceOf(SQLException.class);}finally{db.rollback(point);db.releaseSavepoint(point);}}
}
