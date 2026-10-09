package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.system.api.SystemDictionaryController;
import com.hospital.mes.system.application.DictionaryImportService;
import com.hospital.mes.system.application.DictionaryImportService.Input;
import com.hospital.mes.system.api.SystemDictionaryController.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class SystemDictionaryCrudIT {
 @Autowired SystemDictionaryController dictionaries;
 @Autowired DictionaryImportService imports;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean CurrentPlatformContextResolver contexts;
 private String key(){return UUID.randomUUID().toString();}
 private String code(){return "DICTIONARY_TEST_"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();}
 @BeforeEach void init(){
  long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("SYSTEM_ADMIN"),Set.of("iam:dict:view","iam:dict:create","iam:dict:update"),"dict-integration-test",key()));
  var granted=List.of("iam:dict:view","iam:dict:create","iam:dict:update").stream().map(SimpleGrantedAuthority::new).toList();
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("dict-test","n/a",granted));
 }
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void batchImportPreviewRejectsConflictsAndPersistsOnlyNewTypeInRollback(){
  String unique=code();
  var payload=List.of(new Input(unique,"CSV 导入验证","BUSINESS","FLAT","单次回滚测试",10,"ACTIVE"),
    new Input("EQUIPMENT_TYPE","已有类别","BUSINESS","FLAT",null,20,"ACTIVE"));
  var preview=imports.preview(payload);
  assertThat(preview.accepted()).isEqualTo(1);
  assertThat(preview.rejected()).isEqualTo(1);
  assertThatThrownBy(()->imports.importRows(payload,key())).isInstanceOf(RuntimeException.class);
  assertThat(jdbc.queryForObject("SELECT count(*) FROM sys_dict_type WHERE org_id=1 AND dict_code=?",Long.class,unique)).isZero();
  var result=imports.importRows(List.of(payload.get(0)),key());
  assertThat(result.importedCount()).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT dict_name FROM sys_dict_type WHERE org_id=1 AND dict_code=?",String.class,unique)).isEqualTo("CSV 导入验证");
 }
 @Test void createSearchAndChangeItemWithLiveDictionaryOptions(){
  var created=dictionaries.create(key(),new TypeRequest(code(),"集成测试字典","BUSINESS","TREE","仅事务回滚使用",10,"ACTIVE")).data();
  String typeId=created.get("id").asText();
  var type=dictionaries.get(typeId).data();
  assertThat(type.name()).isEqualTo("集成测试字典");
  var root=dictionaries.createItem(typeId,key(),new ItemRequest("ROOT","主分类","测试节点",null,10,"ACTIVE",false)).data();
  var child=dictionaries.createItem(typeId,key(),new ItemRequest("CHILD","子分类","测试节点",root.get("id").asText(),20,"ACTIVE",false)).data();
  var opts=dictionaries.options(type.code(),null).data();
  assertThat(opts).extracting(Option::value).contains("ROOT","CHILD");
  String itemId=child.get("id").asText();
  assertThat(dictionaries.items(typeId).data()).hasSize(2);
  var updated=dictionaries.updateItem(typeId,itemId,"\"0\"",key(),new ItemRequest("CHILD","改名子分类","测试节点",root.get("id").asText(),20,"INACTIVE",false)).data();
  assertThat(updated.get("label").asText()).isEqualTo("改名子分类");
  assertThat(dictionaries.options(type.code(),null).data()).extracting(Option::value).contains("ROOT").doesNotContain("CHILD");
  assertThat(dictionaries.options(type.code(),"CHILD").data()).anySatisfy(option->{
    assertThat(option.value()).isEqualTo("CHILD");
    assertThat(option.label()).isEqualTo("改名子分类");
    assertThat(option.disabled()).isTrue();
  });
  assertThatThrownBy(()->dictionaries.updateItem(typeId,itemId,"\"0\"",key(),
    new ItemRequest("CHILD","过期更新",null,root.get("id").asText(),20,"ACTIVE",false)))
   .isInstanceOf(RuntimeException.class);
 }
}
