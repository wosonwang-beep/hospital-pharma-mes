package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.system.api.SystemDictionaryController;
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

/** Native DEV rollback tests. No existing dictionary item is modified. */
@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class DictionaryLegacyItemMaintenanceIT {
    @Autowired SystemDictionaryController dictionaries;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CurrentPlatformContextResolver contexts;
    private long actor;
    private String typeId;
    private String typeCode;
    private String itemId;
    private String key() { return UUID.randomUUID().toString(); }
    private void context(long org) {
        when(contexts.current()).thenReturn(new CurrentPlatformContext(org,actor,
            Set.of("SYSTEM_ADMIN"),Set.of("iam:dict:view","iam:dict:create","iam:dict:update"),"dictionary-maintenance",key()));
    }
    @BeforeEach void prepareOwnedFixture() {
        actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name='admin'",Long.class);
        context(1);
        var authorities=List.of("iam:dict:view","iam:dict:create","iam:dict:update")
            .stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("dictionary-maintenance","unused",authorities));
        typeCode="MATERIAL_CLASS_"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
        typeId=dictionaries.create(key(),new TypeRequest(typeCode,"物料分类维护","BUSINESS","FLAT",null,10,"ACTIVE"))
            .data().path("id").asText();
        // Match the historical seed path: V039/V054 have persisted Chinese business codes.
        jdbc.update("INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by) VALUES(1,?,'成品','成品',10,'ACTIVE',?,?)",
            Long.parseLong(typeId),actor,actor);
        itemId=Long.toString(jdbc.queryForObject("SELECT id FROM sys_dict_item WHERE org_id=1 AND dict_type_id=? AND item_code='成品'",Long.class,Long.parseLong(typeId)));
    }
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }
    private ItemRequest change(String code,String state) {
        return new ItemRequest(code,"成品",null,null,10,state,false);
    }
    @Test void existingChineseItemCanBeRetiredWithoutChangingItsIdentity() {
        var saved=dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("成品","INACTIVE")).data();
        assertThat(saved.path("id").asText()).isEqualTo(itemId);
        assertThat(saved.path("code").asText()).isEqualTo("成品");
        assertThat(saved.path("version").asLong()).isEqualTo(1);
        assertThat(saved.path("status").asText()).isEqualTo("INACTIVE");
        assertThat(dictionaries.options(typeCode,null).data()).isEmpty();
        assertThat(dictionaries.options(typeCode,"成品").data()).singleElement().satisfies(option->{
            assertThat(option.value()).isEqualTo("成品");
            assertThat(option.label()).isEqualTo("成品");
            assertThat(option.disabled()).isTrue();
        });
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_dict_item WHERE id=? AND item_code='成品'",Long.class,Long.parseLong(itemId))).isEqualTo(1);
    }
    @Test void legacyCodeStillCannotBeRenamed() {
        assertThatThrownBy(()->dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("FINISHED","INACTIVE")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("immutable");
    }
    @Test void creationStillEnforcesTheExistingNewCodeFormat() {
        assertThatThrownBy(()->dictionaries.createItem(typeId,key(),change("新增分类","ACTIVE")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid dictionary code");
    }
    @Test void invalidStatusStillFailsBeforeAnyMutation() {
        assertThatThrownBy(()->dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("成品","UNKNOWN")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown status");
    }
    @Test void staleVersionStillFails() {
        dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("成品","INACTIVE"));
        assertThatThrownBy(()->dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("成品","ACTIVE")))
            .isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
    }
    @Test void anotherOrganizationCannotMaintainTheLegacyItem() {
        context(987654321L);
        assertThatThrownBy(()->dictionaries.updateItem(typeId,itemId,"\"0\"",key(),change("成品","INACTIVE")))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }
    @Test void fixedQualityObjectCannotBeDisabled() {
        String qc=Long.toString(jdbc.queryForObject("SELECT id FROM sys_dict_type WHERE org_id=1 AND dict_code='QC_OBJECT_TYPE'",Long.class));
        var item=dictionaries.items(qc).data().stream().filter(i->i.code().equals("FINISHED_PRODUCT")).findFirst().orElseThrow();
        var request=new ItemRequest(item.code(),item.label(),item.description(),item.parentId(),item.sortNo(),"INACTIVE",item.isDefault());
        assertThatThrownBy(()->dictionaries.updateItem(qc,item.id(),"\""+item.version()+"\"",key(),request))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("fixed active root codes");
    }
}
