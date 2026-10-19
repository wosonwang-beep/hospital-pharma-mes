package com.hospital.mes.ebr;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.ebr.application.*;
import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.system.infrastructure.SysRolePermissionMapper;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.hospital.mes.ebr.RoleIdIsolationSupport.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK,properties="spring.flyway.enabled=false")
@ActiveProfiles("ci")
class EbrRoleIdIsolatedServiceIT {
 @DynamicPropertySource static void isolatedOnly(DynamicPropertyRegistry r){r.add("spring.datasource.url",RoleIdIsolationSupport::url);r.add("spring.datasource.username",()->System.getenv("MES_ISOLATED_USER"));r.add("spring.datasource.password",()->System.getenv("MES_ISOLATED_PASSWORD"));}
 @Autowired EbrService service; @Autowired EbrDefinitions definitions; @Autowired EbrStore store;
 @Autowired EbrRoleAuthorization authorization; @Autowired MasterMutation mutations;
 @Autowired SysRolePermissionMapper memberships; @Autowired ObjectMapper json;
 @Autowired JdbcTemplate jdbc; @Autowired DataSource dataSource; @Autowired PlatformTransactionManager manager;
 @MockitoBean CurrentPlatformContextResolver contexts;

 @Test void exactDraftConversionUsesServerRolesAndPreservesAllOtherFactsThenRollsBack()throws Exception {
  org.flywaydb.core.Flyway.configure().dataSource(url(),System.getenv("MES_ISOLATED_USER"),System.getenv("MES_ISOLATED_PASSWORD")).locations("classpath:db/migration").target("59").cleanDisabled(true).load().validate();
  Map<String,List<String>> columns;Map<String,List<List<String>>> before;
  try(var c=connect()){assertThat(scalar(c,"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1")).isEqualTo("59");columns=columns(c);before=snapshot(c,columns);}
  long user=11556L;var actor=new CurrentPlatformContext(1,user,Set.copyOf(memberships.activeRoleCodes(user)),Set.copyOf(memberships.activePermissionCodes(user)),"ROLE_ID_ISOLATED","ROLE_ID_ISOLATED",memberships.activeRoleIds(user).stream().map(String::valueOf).collect(java.util.stream.Collectors.toSet()),2);when(contexts.current()).thenReturn(actor);
  var result=new LinkedHashMap<String,Object>();var tx=new TransactionTemplate(manager);tx.setTimeout(60);
  try{
   tx.execute(status->{try{
    var c=DataSourceUtils.getConnection(dataSource);try(var s=c.createStatement()){s.execute("SET SESSION time_zone='+00:00'");}
    assertThat(scalar(c,"SELECT DATABASE()")).isEqualTo(SCHEMA);assertThat(scalar(c,"SELECT @@foreign_key_checks")).isEqualTo("1");
    var locked=store.lock(1,291);assertThat(locked.getStatus()).isEqualTo("DRAFT");assertThat(locked.getVersionNo()).isEqualTo(1L);assertThat(locked.getDefinitionSchemaVersion()).isEqualTo(1);
    var nativeBefore=snapshot(c,columns);assertThat(nativeBefore).isEqualTo(before);var hashes=legacyHashes();
    var current=service.get("291");Definition source=json.convertValue(current.path("definition"),Definition.class);var input=definitions.copyInput(source);
    assertThat(input.signatureRules()).hasSize(7);assertThat(input.reviewRules()).hasSize(7);
    assertThat(input.signatureRules().stream().map(SignatureRule::requiredRoleId)).containsOnly("290","291");
    var saved=service.save("291",new Save(1L,"Authorized isolated role-ID conversion; rollback only",input),"1","ROLE_ID_ISOLATED_"+UUID.randomUUID());
    assertThat(saved.path("status").asText()).isEqualTo("DRAFT");assertThat(saved.path("versionNo").asLong()).isEqualTo(2);assertThat(saved.path("definition").path("schemaVersion").asText()).isEqualTo("2.0");
    var after=snapshot(c,columns);var diffs=checkExactDiff(before,after,columns);
    assertThat(legacyHashes()).isEqualTo(hashes);result.put("legacyHashes",hashes);result.put("changedRows",diffs);result.put("childInsertDeleteCount",0);result.put("targetBusinessRows",15);result.put("maintenanceRows",70);result.put("formJsonSemanticEquality",true);result.put("signatureRowsProtected",before.get("gxp_signature").size());result.put("rootVersion", "1 -> 2 (isolated actual version)");
    return null;
   }catch(Exception e){throw new IllegalStateException(e);}finally{status.setRollbackOnly();}});
  }finally{
   try(var independent=connect()){assertThat(snapshot(independent,columns)).as("Independent full-table rollback verification").isEqualTo(before);}
  }
  result.put("schema",SCHEMA);result.put("businessTransaction","EXPLICIT ROLLBACK; independently verified all tables/rows/cells");result.put("DEVWrites",false);report("isolated-service-result.json",result);
 }
 Map<String,String> legacyHashes(){var out=new TreeMap<String,String>();for(long id:List.of(6L,7L,9L,10L)){var template=store.store(TemplateEntity.class).get(1,id);assertThat(template.getDefinitionSchemaVersion()).isEqualTo(1);String digest=mutations.digest(definitions.canonical(template));assertThat(digest).isEqualTo(template.getContentHash());out.put(String.valueOf(id),digest);}return out;}
 Map<String,Integer> checkExactDiff(Map<String,List<List<String>>> before,Map<String,List<List<String>>> after,Map<String,List<String>> columns)throws Exception {
  Map<String,Set<Long>> expected=Map.of("ebr_template_version",Set.of(291L),"ebr_section_def",range(143,148),"ebr_group_def",range(143,148),"ebr_form_def",range(417,422),"ebr_field_def",range(423,474),"ebr_signature_rule",range(180,186),"ebr_review_rule",range(269,275));
  var diffs=new TreeMap<String,Integer>();var jsonBytes=new TreeMap<String,Boolean>();
  for(String table:before.keySet()){
   var b=before.get(table);var a=after.get(table);
   if(Set.of("gxp_audit_event","platform_idempotency_record").contains(table)){assertThat(a).containsAll(b);assertThat(a.size()-b.size()).isEqualTo(1);continue;}
   if(!expected.containsKey(table)){assertThat(a).as("Untouched table %s",table).isEqualTo(b);continue;}
   Map<String,List<String>> bm=byId(b),am=byId(a);assertThat(am.keySet()).as("Stable IDs %s",table).isEqualTo(bm.keySet());var changed=new HashSet<Long>();var cs=columns.get(table);
   for(String pk:bm.keySet()){
    var old=bm.get(pk);var next=am.get(pk);if(old.equals(next))continue;long rowId=Long.parseLong(text(pk));changed.add(rowId);assertThat(expected.get(table)).contains(rowId);
    Set<String> allowed=new HashSet<>(Set.of("version_no","updated_by","updated_at"));if(table.equals("ebr_template_version"))allowed.add("definition_schema_version");if(table.endsWith("_rule")){allowed.add("required_role_id");allowed.add("required_role_name_snapshot");}if(table.equals("ebr_form_def"))allowed.add("schema_json");
    for(int i=0;i<cs.size();i++)if(!allowed.contains(cs.get(i)))assertThat(next.get(i)).as("%s/%s/%s",table,rowId,cs.get(i)).isEqualTo(old.get(i));
    int v=cs.indexOf("version_no");assertThat(Long.parseLong(text(next.get(v)))).isEqualTo(Long.parseLong(text(old.get(v)))+1);
    if(table.equals("ebr_form_def")){int j=cs.indexOf("schema_json");assertThat(json.readTree(text(next.get(j)))).as("Strict JSON semantics form %s",rowId).isEqualTo(json.readTree(text(old.get(j))));jsonBytes.put(String.valueOf(rowId),Objects.equals(old.get(j),next.get(j)));assertThat(text(next.get(cs.indexOf("schema_version")))).isEqualTo("1.0");}
    if(table.endsWith("_rule")){long role=(rowId==186||rowId==275)?291:290;assertThat(text(next.get(cs.indexOf("required_role_id")))).isEqualTo(String.valueOf(role));assertThat(text(next.get(cs.indexOf("required_role_name_snapshot")))).isEqualTo(jdbc.queryForObject("SELECT display_name FROM sys_role WHERE id=?",String.class,role));assertThat(text(next.get(cs.indexOf("required_role")))).isEqualTo(jdbc.queryForObject("SELECT role_code FROM sys_role WHERE id=?",String.class,role));}
   }
   assertThat(changed).as("Exact row candidates %s",table).isEqualTo(expected.get(table));diffs.put(table,changed.size());
  }
  assertThat(diffs.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(85);report("isolated-form-json-byte-comparison.json",jsonBytes);return diffs;
 }
 private static Set<Long> range(long start,long end){var result=new HashSet<Long>();for(long i=start;i<=end;i++)result.add(i);return result;}
 private static String text(String hex){return hex==null?null:new String(HexFormat.of().parseHex(hex),StandardCharsets.UTF_8);}
 private static Map<String,List<String>> byId(List<List<String>> rows){var out=new TreeMap<String,List<String>>();for(var row:rows)assertThat(out.put(row.getFirst(),row)).isNull();return out;}
}
