package com.hospital.mes.process.integration;

import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Committed unique fixtures enable actual concurrent connections; cleanup is limited to their exact IDs. */
@SpringBootTest @ActiveProfiles("ci")
class BasicMaintenanceConcurrencyIT {
 @Autowired UnitService units;
 @Autowired JdbcTemplate jdbc;
 @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts;
 @Test void rowLockSerializesCurrentSavesWithoutClientVersionPreconditions() throws Exception {
  String suffix=UUID.randomUUID().toString().replace("-","").substring(0,12),login="nv_lock_"+suffix;
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,'test-only',TRUE,FALSE)",login,login,"Unique concurrency fixture");
  long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("NV_TEST"),Set.of("master:uom:create","master:uom:update","master:uom:view"),"nv-lock",suffix));
  String id=null;var executor=Executors.newFixedThreadPool(2);var locked=new CountDownLatch(1);var release=new CountDownLatch(1);
  try {
   id=units.create(new UnitCommands.Create("NVLOCK"+suffix,"Initial","MASS",3),UUID.randomUUID().toString()).path("id").asText();
   String target=id;
   var first=executor.submit(()->new TransactionTemplate(transactions).execute(tx->{
    jdbc.queryForObject("SELECT id FROM md_unit WHERE id=? FOR UPDATE",Long.class,target);locked.countDown();
    try {if(!release.await(5,TimeUnit.SECONDS))throw new IllegalStateException("Lock release timeout");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
    return units.command(target,new UnitCommands.Update("UPDATE",null,null,"First","MASS",3),null,UUID.randomUUID().toString());
   }));
   assertThat(locked.await(5,TimeUnit.SECONDS)).isTrue();
   var second=executor.submit(()->units.command(target,new UnitCommands.Update("UPDATE",null,999L,"Second","MASS",3),null,UUID.randomUUID().toString()));
   assertThatThrownBy(()->second.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
   release.countDown();first.get(5,TimeUnit.SECONDS);second.get(5,TimeUnit.SECONDS);
   assertThat(units.get(target).path("unitName").asText()).isEqualTo("Second");
   assertThat(jdbc.queryForObject("SELECT version_no FROM md_unit WHERE id=?",Long.class,target)).isZero();
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE actor_id=?",Long.class,actor)).isZero();
  } finally {
   release.countDown();executor.shutdown();assertThat(executor.awaitTermination(10,TimeUnit.SECONDS)).isTrue();
   jdbc.update("DELETE FROM platform_idempotency_record WHERE actor_id=?",actor);
   if(id!=null)jdbc.update("DELETE FROM md_unit WHERE id=? AND unit_code=?",id,"NVLOCK"+suffix);
   jdbc.update("DELETE FROM sys_user WHERE id=? AND login_name_normalized=?",actor,login);
  }
 }
}
