package com.hospital.mes.release;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.release.application.FinishedReleaseService;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Read-only concurrency acceptance against the existing approved DEV batch; no fixture writes. */
@SpringBootTest
@ActiveProfiles("ci")
@EnabledIfSystemProperty(named="mes.qa.readonly-lock-fixture",matches="true")
class QaReadOnlyReviewLockIT {
 @Autowired FinishedReleaseService release;
 @Autowired JdbcTemplate jdbc;
 @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts;
 @BeforeEach void adminContext(){
  long admin=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class);
  var permissions=Set.of("qa:batch-review","qa:release","ebr:sign","ebr:form:view","balance:view","qms:deviation:view","qms:test:view","qms:finished-request:view","qms:finished-sampling:view","qms:finished-report:view","wms:finished-inbound:view");
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,admin,Set.of("SYSTEM_ADMIN"),permissions,"qa-readonly-lock","qa-readonly-lock"));
 }
 @Test void readOnlyReviewDoesNotWaitForHeldBusinessLocksAndMatchesLockedReview() throws Exception {
  long batch=Long.parseLong(System.getProperty("mes.qa.readonly-lock-fixture.batch-id","655"));
  var root=jdbc.queryForMap("SELECT id,production_order_id,status FROM prd_main_batch WHERE id=?",batch);
  assertThat(root.get("status").toString()).isEqualTo("QA_RELEASED");
  long order=((Number)root.get("production_order_id")).longValue();
  long quantityBefore=jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Long.class,batch);
  var writer=new TransactionTemplate(transactions);writer.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  var reader=new TransactionTemplate(transactions);reader.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);reader.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);reader.setReadOnly(true);
  long unblockedStart=System.nanoTime();
  JsonNode unblockedReview=reader.execute(status->release.review(Long.toString(batch)));
  long unblockedMillis=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-unblockedStart);
  assertThat(unblockedReview.path("gates")).hasSize(8);
  System.out.println("QA_READONLY_UNBLOCKED_BASELINE_MS "+unblockedMillis);
  var locked=new CountDownLatch(1);var releaseLocks=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  Future<?> holder=pool.submit(()->writer.execute(status->{
   lock("prd_production_order","id",order);lock("prd_main_batch","id",batch);
   jdbc.queryForList("SELECT o.id FROM mes_operation_execution o JOIN prd_execution_unit e ON e.id=o.execution_unit_id WHERE e.main_batch_id=? FOR UPDATE",Long.class,batch);lock("qms_deviation","main_batch_id",batch);
   lock("wms_finished_inventory_ledger","main_batch_id",batch);lock("wms_finished_inbound_request","main_batch_id",batch);
   lock("qms_finished_inspection_request","main_batch_id",batch);lock("qms_finished_sampling_record","main_batch_id",batch);
   jdbc.queryForList("SELECT r.id FROM qms_finished_inspection_report r JOIN qms_finished_inspection_request q ON q.id=r.inspection_request_id WHERE q.main_batch_id=? FOR UPDATE",Long.class,batch);
   locked.countDown();try{releaseLocks.await(2500,TimeUnit.MILLISECONDS);}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}return null;
  }));
  try {
   assertThat(locked.await(5,TimeUnit.SECONDS)).isTrue();long started=System.nanoTime();
   var executing=new AtomicReference<Thread>();
      Future<JsonNode> read=pool.submit(()->{executing.set(Thread.currentThread());return reader.execute(status->release.review(Long.toString(batch)));});

      JsonNode actual;
      try{actual=read.get(6,TimeUnit.SECONDS);}catch(TimeoutException timeout){
       var target=executing.get();System.err.println("QA_READONLY_TIMEOUT_READER_STACK_BEGIN");
       if(target!=null)for(var frame:target.getStackTrace())System.err.println("QA_READER_FRAME "+frame);
       System.err.println("QA_READONLY_TIMEOUT_READER_STACK_END");throw timeout;
      }long elapsed=System.nanoTime()-started;
   System.out.println("QA_READONLY_UNDER_HELD_LOCKS_MS "+TimeUnit.NANOSECONDS.toMillis(elapsed));
   assertThat(Duration.ofNanos(elapsed).toMillis()).as("read-only QA review must not wait on writer locks").isLessThan(1800);
   assertThat(actual.path("status").asText()).isEqualTo("QA_RELEASED");assertThat(actual.path("gates")).hasSize(8);
   releaseLocks.countDown();holder.get(5,TimeUnit.SECONDS);
   var authoritative=new TransactionTemplate(transactions);authoritative.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
   JsonNode lockedReview=authoritative.execute(status->release.review(Long.toString(batch)));
   assertThat(actual.path("gates")).isEqualTo(lockedReview.path("gates"));
   assertThat(actual.path("blockingCodes")).isEqualTo(lockedReview.path("blockingCodes"));
   assertThat(actual.path("allowedActions")).isEqualTo(lockedReview.path("allowedActions"));
   assertThat(actual.path("reviewDigest")).isEqualTo(lockedReview.path("reviewDigest"));
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Long.class,batch)).isEqualTo(quantityBefore);
  } finally {releaseLocks.countDown();pool.shutdownNow();pool.awaitTermination(5,TimeUnit.SECONDS);}
 }
 private void lock(String table,String column,long value){jdbc.queryForList("SELECT id FROM "+table+" WHERE "+column+"=? FOR UPDATE",Long.class,value);}
}
