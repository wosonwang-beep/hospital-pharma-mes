package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.mockito.Mockito.*;

/** Real registered-provider sample and rollback-only source setup for existing print lifecycle tests. */
final class PrintSourceIntegrationFixture {
 static JsonNode configuration(ObjectMapper json,PrintDataProvider provider){var n=json.createObjectNode();n.put("schemaVersion",1).put("sourceName","受控全字段回滚测试").put("mainCatalogId","provider."+provider.businessType());n.putArray("parameterDeclarations").addObject().put("name","recordId").put("source","DOCUMENT_ID").putNull("contextKey");var joins=n.putArray("joins");var j=joins.addObject();j.put("nodeId","lines").put("label",provider.documentLabel()+"明细").put("parentNodeId","main").put("catalogId","provider."+provider.businessType()+".items").put("knownRelationId","provider."+provider.businessType()+".itemsRelation").put("cardinality","1:N").put("datasetKey","items").put("displayOrder",0);j.putArray("keys").addObject().put("parentFieldId","id").put("targetFieldId","id");var fields=n.putArray("fields");int order=0;for(var f:provider.fieldDefinitions()){var v=fields.addObject();v.put("fieldId","F"+f.key()).put("fieldKey",f.key()).put("label",f.label()).put("dataType",f.dataType()).put("displayOrder",order++).put("dataGroup",f.repeated()?"items":"MAIN").put("missing","EMPTY");if(f.repeated())v.put("datasetKey","items");else v.putNull("datasetKey");v.putObject("sourcePath").put("nodeId",f.repeated()?"lines":"main").put("catalogFieldId",f.key());}return n;}
 static String key(){return UUID.randomUUID().toString();}
 static JsonNode ensure(PrintDataSourceService sources,PrintService printing,ObjectMapper json,JdbcTemplate jdbc,CurrentPlatformContextResolver contexts,long actor,Set<String> original,String type,String sample){
  var rights=new HashSet<>(original);rights.addAll(Set.of("print:source:view","print:source:manage","print:source:publish"));rights.addAll(printing.provider(type).businessReadPermissions());when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("TEST"),rights,"source-fixture","source-fixture"));
  try{try{var fixed=sources.fixed(1,type,null);return sources.detail(fixed.source().getId().toString());}catch(ComplianceException error){if(!Set.of("PRINT_SOURCE_UNBOUND","PRINT_SOURCE_NOT_PUBLISHED").contains(error.code()))throw error;}
   JsonNode old=null;try{old=sources.getMetadata(type);}catch(NoSuchElementException ignored){}var metadata=json.createObjectNode().put("expectedVersion",old==null?0:old.path("versionNo").asLong()).put("authorizationPolicyRef","provider."+type).put("reason","独立测试记录；事务回滚");metadata.putArray("compatibleMainCatalogIds").add("provider."+type);metadata.putArray("fixedTypeFilters");sources.putMetadata(type,metadata,metadata.path("expectedVersion").asText(),key());
   var create=json.createObjectNode().put("sourceCode","IT"+key().replace("-","")).put("sourceName","受控全字段回滚测试").put("reason","独立测试记录；事务回滚");create.putArray("businessTypes").add(type);create.set("configuration",configuration(json,printing.provider(type)));var source=sources.save(null,create,null,key());String id=source.path("id").asText();var validation=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("reason","真实业务样本读取；不提交正文");validation.putArray("samples").addObject().put("businessType",type).put("recordId",sample);var proof=sources.validate(id,validation,source.path("versionNo").asText(),key());if(!proof.path("result").asText().equals("PASS"))throw new AssertionError(proof.toPrettyString());var publish=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("revisionId",source.path("draft").path("id").asText()).put("configHash",source.path("draft").path("configHash").asText()).put("validationId",proof.path("id").asText()).put("reason","回滚测试发布");return sources.publish(id,publish,source.path("versionNo").asText(),key());
  }finally{when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("TEST"),original,"source-fixture","source-fixture"));}
 }
}
