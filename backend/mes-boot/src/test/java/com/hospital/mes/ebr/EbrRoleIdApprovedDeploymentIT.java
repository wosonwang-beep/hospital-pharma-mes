package com.hospital.mes.ebr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.ebr.application.EbrDefinitions;
import com.hospital.mes.ebr.infrastructure.EbrStore;
import com.hospital.mes.masterdata.application.MasterMutation;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import static org.assertj.core.api.Assertions.*;
import static com.hospital.mes.ebr.RoleIdIsolationSupport.*;

/** Temporary acceptance runtime only: real security, real HTTP8080, existing DEV.
 * This configuration is test-source-only and never packaged in the production JAR.
 * The sole approved PUT is buffered until exact precommit validation succeeds.
 */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT,properties={"server.port=8080","spring.flyway.enabled=false"})
@ActiveProfiles("local")
@Import(EbrRoleIdApprovedDeploymentIT.GuardConfiguration.class)
class EbrRoleIdApprovedDeploymentIT {
 static String approvedUrl(){assertThat(System.getenv("MES_ROLE_DEPLOY_APPROVAL")).isEqualTo("Sentinel_28d083eeadf08191b027ac092db1e49d");String value=System.getenv("MES_ROLE_DEPLOY_URL");assertThat(value).isEqualTo("jdbc:mariadb://127.0.0.1:3306/hospital_pharma_mes_dev");return value;}
 @DynamicPropertySource static void exactDev(DynamicPropertyRegistry r){r.add("spring.datasource.url",EbrRoleIdApprovedDeploymentIT::approvedUrl);r.add("spring.datasource.username",()->System.getenv("MES_ROLE_DEPLOY_USER"));r.add("spring.datasource.password",()->System.getenv("MES_ROLE_DEPLOY_PASSWORD"));}
 @Autowired ApprovedPutGuard guard;
 @Test void serveApprovedRealUiConversionUntilExplicitFinishMarker()throws Exception {
  org.flywaydb.core.Flyway.configure().dataSource(approvedUrl(),System.getenv("MES_ROLE_DEPLOY_USER"),System.getenv("MES_ROLE_DEPLOY_PASSWORD")).locations("classpath:db/migration").target("59").cleanDisabled(true).load().validate();
  Path local=Path.of(System.getenv("MES_ROLE_DEPLOY_PRIVATE"));Files.writeString(local.resolve("GUARDED_RUNTIME_READY.txt"),"Real current-source local8080; exact approved PUT guard active; no authentication mocks");
  long deadline=System.nanoTime()+java.time.Duration.ofMinutes(15).toNanos();
  while(!Files.exists(local.resolve("UI_TEST_FINISHED.txt"))&&System.nanoTime()<deadline)Thread.sleep(250);
  assertThat(Files.exists(local.resolve("UI_TEST_FINISHED.txt"))).as("Explicit local UI finish marker").isTrue();
  assertThat(guard.committed.get()).as("Only after actual database commit").isTrue();
 }
 @TestConfiguration static class GuardConfiguration {
  @Bean ApprovedPutGuard approvedPutGuard(DataSource dataSource,JdbcTemplate jdbc,ObjectMapper json,PlatformTransactionManager tx,EbrStore store,EbrDefinitions definitions,MasterMutation mutations){return new ApprovedPutGuard(dataSource,jdbc,json,tx,store,definitions,mutations);}
  @Bean FilterRegistrationBean<ApprovedPutGuard> approvedGuardRegistration(ApprovedPutGuard guard){var registration=new FilterRegistrationBean<>(guard);registration.setOrder(-99);return registration;}
 }
 static class ApprovedPutGuard extends OncePerRequestFilter {
  final DataSource source;final JdbcTemplate jdbc;final ObjectMapper json;final TransactionTemplate tx;final EbrRoleIdIsolatedServiceIT checker;final AtomicBoolean committed=new AtomicBoolean();
  ApprovedPutGuard(DataSource source,JdbcTemplate jdbc,ObjectMapper json,PlatformTransactionManager manager,EbrStore store,EbrDefinitions definitions,MasterMutation mutations){this.source=source;this.jdbc=jdbc;this.json=json;tx=new TransactionTemplate(manager);tx.setTimeout(60);checker=new EbrRoleIdIsolatedServiceIT();checker.json=json;checker.jdbc=jdbc;checker.store=store;checker.definitions=definitions;checker.mutations=mutations;}
  @Override protected boolean shouldNotFilter(HttpServletRequest r){return !r.getMethod().equals("PUT")||!r.getRequestURI().equals("/api/v1/ebr/templates/291");}
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {
   var held=new ContentCachingResponseWrapper(response);
   if(committed.get()){held.setStatus(409);held.getWriter().write("Approved conversion already committed; do not replay");held.copyBodyToResponse();return;}
   try{
    tx.execute(status->{boolean verified=false;try{
     Connection c=DataSourceUtils.getConnection(source);try(var s=c.createStatement()){s.execute("SET SESSION time_zone='+00:00'");}
     assertThat(scalar(c,"SELECT DATABASE()")).isEqualTo("hospital_pharma_mes_dev");assertThat(scalar(c,"SELECT @@foreign_key_checks")).isEqualTo("1");
     var root=checker.store.lock(1,291);assertThat(root.getStatus()).isEqualTo("DRAFT");assertThat(root.getVersionNo()).isEqualTo(2L);assertThat(root.getDefinitionSchemaVersion()).isEqualTo(1);
     assertThat(request.getHeader("If-Match")).isIn("2","\"2\"");String key=request.getHeader("Idempotency-Key");assertThat(key).isNotBlank();
     assertThat(scalar(c,"SELECT COUNT(*) FROM ebr_signature_rule WHERE template_version_id=291 AND version_no=1 AND required_role_id IS NULL")).isEqualTo("7");assertThat(scalar(c,"SELECT COUNT(*) FROM ebr_review_rule WHERE template_version_id=291 AND version_no=1 AND required_role_id IS NULL")).isEqualTo("7");
     var columns=columns(c);var before=snapshot(c,columns);var oldHashes=checker.legacyHashes();
     chain.doFilter(request,held);
     if(held.getStatus()>=400){status.setRollbackOnly();return null;}
     var payload=json.readTree(held.getContentAsByteArray()).path("data");assertThat(payload.path("id").asText()).isEqualTo("291");assertThat(payload.path("status").asText()).isEqualTo("DRAFT");assertThat(payload.path("versionNo").asLong()).isEqualTo(3);assertThat(payload.path("definition").path("schemaVersion").asText()).isEqualTo("2.0");
     var after=snapshot(c,columns);var diff=checker.checkExactDiff(before,after,columns);assertThat(checker.legacyHashes()).isEqualTo(oldHashes);
     int formJsonColumn=columns.get("ebr_form_def").indexOf("schema_json");
     for(int formId=417;formId<=422;formId++){String pk=java.util.HexFormat.of().formatHex(String.valueOf(formId).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toUpperCase();var oldForm=before.get("ebr_form_def").stream().filter(row->pk.equals(row.getFirst())).findFirst().orElseThrow();var newForm=after.get("ebr_form_def").stream().filter(row->pk.equals(row.getFirst())).findFirst().orElseThrow();assertThat(newForm.get(formJsonColumn)).as("Approved exact schema_json bytes form %s",formId).isEqualTo(oldForm.get(formJsonColumn));}
     var receipt=new LinkedHashMap<String,Object>();receipt.put("schema","hospital_pharma_mes_dev");receipt.put("requestUri",request.getRequestURI());receipt.put("method","PUT");receipt.put("idempotencyKey",key);receipt.put("precommitValidated",true);receipt.put("changedRows",diff);receipt.put("targetBusinessRows",15);receipt.put("maintenanceRows",70);receipt.put("childInsertDeleteCount",0);receipt.put("legacyHashes",oldHashes);receipt.put("signatureRowsProtected",before.get("gxp_signature").size());receipt.put("rootVersion","2 -> 3");receipt.put("allOtherRowsCellsPreserved",true);
     Path local=Path.of(System.getenv("MES_ROLE_DEPLOY_PRIVATE"));JSON.writerWithDefaultPrettyPrinter().writeValue(local.resolve("precommit-all-rows-private.json").toFile(),Map.of("columns",columns,"before",before,"after",after));
     report("DEV_PRECOMMIT_VALIDATION.json",receipt);
     TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){committed.set(true);receipt.put("transaction","COMMITTED; afterCommit callback reached");try{report("DEV_COMMIT_RECEIPT.json",receipt);Files.writeString(local.resolve("COMMIT_CONFIRMED.txt"),key);}catch(Exception e){throw new IllegalStateException("Commit happened; receipt uncertain; independently verify and never replay",e);}}});
     verified=true;return null;
    }catch(Exception e){throw new IllegalStateException(e);}finally{if(!verified)status.setRollbackOnly();}});
    // The real network success response is released only after the guarded outer commit.
    held.copyBodyToResponse();
   }catch(RuntimeException|AssertionError e){try{report("DEV_TRANSACTION_FAILURE.json",Map.of("type",e.getClass().getName(),"firstLine",String.valueOf(e.getMessage()).split("\n",2)[0],"commitCallbackReached",committed.get(),"action","Independently verify actual state; never replay an uncertain commit"));}catch(Exception logError){e.addSuppressed(logError);}throw new ServletException("Approved conversion transaction failed; see private evidence",e);}
  }
 }
}
