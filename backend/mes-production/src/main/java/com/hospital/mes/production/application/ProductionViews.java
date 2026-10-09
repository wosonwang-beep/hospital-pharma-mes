package com.hospital.mes.production.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
@org.springframework.stereotype.Component
public class ProductionViews {
 private final ObjectMapper json;public ProductionViews(ObjectMapper json){this.json=json;}
 /** API-only detached projection; never traverse or alter a signature's canonical envelope. */
 public JsonNode read(Object value){JsonNode copy=json.valueToTree(value);project(copy);return copy;}
 private void project(JsonNode copy){if(copy.isArray()){copy.forEach(this::project);return;}if(!copy.isObject())return;for(var evidence:copy.path("gateEvidence"))if(evidence instanceof ObjectNode object)object.retain(java.util.List.of("action","checkedAt","actorId","configurationHash","qualifications"));}
 public ObjectNode view(ScopedEntity row){var out=(ObjectNode)json.valueToTree(row);var bean=new org.springframework.beans.BeanWrapperImpl(row);for(var prop:bean.getPropertyDescriptors()){String name=prop.getName();if(!out.has(name)||out.get(name).isNull())continue;Object value=bean.getPropertyValue(name);if(value instanceof java.math.BigDecimal d)out.put(name,d.toPlainString());else if(value instanceof Long&& (name.equals("id")||name.endsWith("Id")||name.endsWith("By")))out.put(name,value.toString());else if(value instanceof LocalDateTime time)out.put(name,time.toInstant(ZoneOffset.UTC).toString());else if(value instanceof LocalDate date)out.put(name,date.toString());else if(name.endsWith("Json")&&value instanceof String text){try{out.set(name.substring(0,name.length()-4),json.readTree(text));out.remove(name);}catch(java.io.IOException ex){throw new IllegalStateException("Stored JSON invalid",ex);}}}out.remove(java.util.List.of("orgId","createdBy","updatedBy"));out.putArray("allowedActions");if(row instanceof com.hospital.mes.production.infrastructure.SnapshotEntity)out.retain(java.util.List.of("id","packageVersionId","processPackageId","formulaVersionId","routeVersionId","ebrTemplateVersionId","snapshotHash","snapshot"));return out;}
}
