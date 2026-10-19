package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import com.hospital.mes.masterdata.application.MasterMutation;
import java.util.*;
import org.springframework.beans.BeanWrapperImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Readonly DEV retained-row capture; new serializer must reproduce all four existing frozen hashes. */
class EbrRetainedPublishedRoleSchemaTest {
    @ParameterizedTest @ValueSource(longs={6,7,9,10})
    void retainsExactPublishedCanonicalHash(long id)throws Exception{
        ObjectMapper json=new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonNode fixture;try(var stream=getClass().getResourceAsStream("/role-id-retained-published-20261011.json")){assertNotNull(stream);fixture=json.readTree(stream);}
        Map<Class<?>,List<ScopedEntity>> rows=new HashMap<>();
        for(Class<? extends ScopedEntity> type:List.of(TemplateEntity.class,SectionEntity.class,GroupEntity.class,FormEntity.class,FieldEntity.class,OptionEntity.class,RuleEntity.class,SignatureRuleEntity.class,ReviewRuleEntity.class)){
            List<ScopedEntity> data=new ArrayList<>();for(JsonNode raw:fixture.path(type.getSimpleName())){ObjectNode node=(ObjectNode)raw.deepCopy();if(node.path("schemaJson").isObject())node.put("schemaJson",node.path("schemaJson").toString());data.add(json.treeToValue(node,type));}rows.put(type,data);
        }
        EbrStore db=mock(EbrStore.class);
        when(db.rows(any(),anyLong(),anyString(),any())).thenAnswer(call->{Class<?> type=call.getArgument(0);long org=call.getArgument(1);String column=call.getArgument(2);Object parent=call.getArgument(3);String property=column.replaceAll("_([a-z])","_$1");var parts=column.split("_");property=parts[0];for(int i=1;i<parts.length;i++)property+=Character.toUpperCase(parts[i].charAt(0))+parts[i].substring(1);final String key=property;return rows.getOrDefault(type,List.of()).stream().filter(r->r.getOrgId()==org&&Objects.equals(new BeanWrapperImpl(r).getPropertyValue(key),parent)).toList();});
        TemplateEntity template=(TemplateEntity)rows.get(TemplateEntity.class).stream().filter(r->r.getId()==id).findFirst().orElseThrow();
        EbrDefinitions definitions=new EbrDefinitions(db,json,null,null,null);JsonNode canonical=definitions.canonical(template);
        assertEquals("1.0",canonical.path("schemaVersion").asText());assertFalse(canonical.path("definition").has("schemaVersion"));
        assertEquals(template.getContentHash(),new MasterMutation(null,null,null,json).digest(canonical));
        verify(db,never()).store(any());
    }
}
