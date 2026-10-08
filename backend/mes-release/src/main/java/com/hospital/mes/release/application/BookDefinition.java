package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import com.hospital.mes.masterdata.application.MasterMutation;
/** Closed, portable directory contract; no executable source expressions. */
public record BookDefinition(String varietyCode,String varietyName,List<Mapping> mappings,List<Entry> entries) {
 public record Mapping(String productId,String packageVersionId,String ebrTemplateVersionId){}
 public record Entry(String code,String chapter,String title,String kind,int order,boolean required,int minCount,String scope,String operationCode,String formCode,String printTemplateVersionId,List<String> fields,List<String> attachmentIds){}
 public static final Set<String> KINDS=Set.of("FORM","PROCESS_INSTRUCTIONS","PRODUCTION_ORDER","MATERIAL_ISSUES","CHARGES","CLEARANCE","INSPECTION_REPORTS","ATTACHMENTS");
 public BookDefinition {mappings=List.copyOf(mappings);entries=List.copyOf(entries);}
 public Set<String> batchFormCodes(){var result=new TreeSet<String>();entries.stream().filter(e->e.kind().equals("FORM")&&e.scope().equals("BATCH")).forEach(e->result.add(e.formCode()));return Set.copyOf(result);}
 public List<Entry> ordered(){return entries.stream().sorted(Comparator.comparingInt(Entry::order).thenComparing(Entry::code)).toList();}
 public static BookDefinition parse(JsonNode n,ObjectMapper json){
  exact(n,Set.of("varietyCode","varietyName","mappings","entries"));code(n.path("varietyCode").asText());text(n.path("varietyName").asText(),120);
  if(!n.path("mappings").isArray()||n.path("mappings").isEmpty()||n.path("mappings").size()>100||!n.path("entries").isArray()||n.path("entries").isEmpty()||n.path("entries").size()>80)throw invalid();
  var products=new HashSet<String>();for(var m:n.path("mappings")){exact(m,Set.of("productId","packageVersionId","ebrTemplateVersionId"));for(String k:List.of("productId","packageVersionId","ebrTemplateVersionId"))MasterMutation.id(m.path(k).asText());if(!products.add(m.path("productId").asText()))throw invalid();}
  var codes=new HashSet<String>();var orders=new HashSet<Integer>();var forms=new HashSet<String>();
  for(var e:n.path("entries")){exact(e,Set.of("code","chapter","title","kind","order","required","minCount","scope","operationCode","formCode","printTemplateVersionId","fields","attachmentIds"));code(e.path("code").asText());text(e.path("chapter").asText(),120);text(e.path("title").asText(),160);if(!codes.add(e.path("code").asText())||!e.path("order").isIntegralNumber()||e.path("order").asInt()<1||!orders.add(e.path("order").asInt())||!e.path("required").isBoolean()||!e.path("minCount").isIntegralNumber()||e.path("minCount").asInt()<0||e.path("minCount").asInt()>1000||e.path("required").asBoolean()&&e.path("minCount").asInt()<1||!KINDS.contains(e.path("kind").asText())||!Set.of("BATCH","OPERATION").contains(e.path("scope").asText()))throw invalid();
   if(!e.path("fields").isArray()||e.path("fields").size()>200||!e.path("attachmentIds").isArray()||e.path("attachmentIds").size()>100)throw invalid();var fs=new HashSet<String>();for(var f:e.path("fields")){code(f.asText());if(!fs.add(f.asText()))throw invalid();}for(var a:e.path("attachmentIds"))MasterMutation.id(a.asText());
   if(e.path("kind").asText().equals("FORM")){code(e.path("formCode").asText());code(e.path("operationCode").asText());if(e.path("fields").isEmpty()||!forms.add(e.path("formCode").asText()))throw invalid();}else if(e.hasNonNull("formCode")||e.hasNonNull("printTemplateVersionId"))throw invalid();
   if(e.hasNonNull("printTemplateVersionId"))MasterMutation.id(e.path("printTemplateVersionId").asText());
  }
  try{return json.treeToValue(n,BookDefinition.class);}catch(Exception ex){throw new IllegalArgumentException("Invalid closed book definition",ex);}
 }
 private static void exact(JsonNode n,Set<String> keys){if(!n.isObject())throw invalid();n.fieldNames().forEachRemaining(k->{if(!keys.contains(k))throw new IllegalArgumentException("Unknown book field: "+k);});}
 private static void code(String s){if(!s.matches("[A-Za-z][A-Za-z0-9_-]{0,63}"))throw invalid();}
 private static void text(String s,int max){if(s.isBlank()||s.length()>max||s.chars().anyMatch(Character::isISOControl))throw invalid();}
 private static IllegalArgumentException invalid(){return new IllegalArgumentException("Invalid book directory, mapping, field or scope");}
}
