package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import com.hospital.mes.masterdata.application.MasterMutation;

/** Closed electronic batch-record template contract. New templates bind production process; legacy product mappings remain readable. */
public record BookDefinition(
 String templateName,String processPackageId,String ebrTemplateVersionId,
 String varietyCode,String varietyName,List<Mapping> mappings,List<Entry> entries
) {
 public record Mapping(String productId,String packageVersionId,String ebrTemplateVersionId,String processPackageId){
  public Mapping(String productId,String packageVersionId,String ebrTemplateVersionId){this(productId,packageVersionId,ebrTemplateVersionId,null);}
  public String processKey(){return processPackageId!=null?"CURRENT:"+processPackageId:"LEGACY:"+packageVersionId;}
 }
 public record Entry(
  String code,String flowType,String flowCode,String flowName,String chapter,
  String title,String kind,int order,boolean required,int minCount,String scope,
  String operationCode,String formCode,String printTemplateVersionId,List<String> fields,List<String> attachmentIds,String archiveStage,String applicability
 ){
  public Entry(String code,String chapter,String title,String kind,int order,boolean required,int minCount,String scope,String operationCode,String formCode,String printTemplateVersionId,List<String> fields,List<String> attachmentIds){this(code,null,null,null,chapter,title,kind,order,required,minCount,scope,operationCode,formCode,printTemplateVersionId,fields,attachmentIds,null,null);}
  public String directoryGroup(){return flowName!=null&&!flowName.isBlank()?flowName:chapter;}
  public boolean modern(){return flowType!=null;}
 }
 public static final Set<String> KINDS=Set.of("FORM","PROCESS_INSTRUCTIONS","PRODUCTION_ORDER","MATERIAL_REQUESTS","MATERIAL_ISSUES","CHARGES","CLEARANCE","INSPECTION_REQUESTS","SAMPLING_RECORDS","INSPECTION_RECORDS","INSPECTION_REPORTS","QUALITY_INVESTIGATIONS","MATERIAL_BALANCE","QA_DECISIONS","ATTACHMENTS");
 public static final Set<String> FLOW_TYPES=Set.of("SYSTEM","PROCESS","BUSINESS");

 public BookDefinition{
  mappings=List.copyOf(mappings==null?List.of():mappings);
  entries=List.copyOf(entries==null?List.of():entries);
 }
 public boolean modern(){return processPackageId!=null;}
 public String displayName(){return templateName!=null&&!templateName.isBlank()?templateName:varietyName;}
 public Set<String> batchFormCodes(){var result=new TreeSet<String>();entries.stream().filter(e->e.kind().equals("FORM")&&e.scope().equals("BATCH")).forEach(e->result.add(e.formCode()));return Set.copyOf(result);}
 public List<Entry> ordered(){return entries.stream().sorted(Comparator.comparingInt(Entry::order).thenComparing(Entry::code)).toList();}

 public static BookDefinition parse(JsonNode n,ObjectMapper json){
  if(!n.isObject())throw invalid();
  boolean modern=n.hasNonNull("processPackageId")||n.hasNonNull("templateName")||n.hasNonNull("ebrTemplateVersionId");
  if(modern){
   exact(n,Set.of("templateName","processPackageId","ebrTemplateVersionId","entries"));
   text(n.path("templateName").asText(),120);
   MasterMutation.id(n.path("processPackageId").asText());
   MasterMutation.id(n.path("ebrTemplateVersionId").asText());
  }else{
   exact(n,Set.of("varietyCode","varietyName","mappings","entries"));
   code(n.path("varietyCode").asText());text(n.path("varietyName").asText(),120);
   if(!n.path("mappings").isArray()||n.path("mappings").isEmpty()||n.path("mappings").size()>100)throw invalid();
   var products=new HashSet<String>();
   for(var m:n.path("mappings")){
    exact(m,Set.of("productId","packageVersionId","processPackageId","ebrTemplateVersionId"));
    if(m.hasNonNull("processPackageId")==m.hasNonNull("packageVersionId"))throw invalid();
    for(String k:List.of("productId",m.hasNonNull("processPackageId")?"processPackageId":"packageVersionId","ebrTemplateVersionId"))MasterMutation.id(m.path(k).asText());
    if(!products.add(m.path("productId").asText()))throw invalid();
   }
  }
  if(!n.path("entries").isArray()||n.path("entries").isEmpty()||n.path("entries").size()>120)throw invalid();
  var codes=new HashSet<String>();var orders=new HashSet<Integer>();var forms=new HashSet<String>();
  for(var e:n.path("entries")){
   exact(e,Set.of("code","flowType","flowCode","flowName","chapter","title","kind","order","required","minCount","scope","operationCode","formCode","printTemplateVersionId","fields","attachmentIds","archiveStage","applicability"));
   code(e.path("code").asText());text(e.path("title").asText(),160);
   if(modern){
    if(!FLOW_TYPES.contains(e.path("flowType").asText()))throw invalid();
    code(e.path("flowCode").asText());text(e.path("flowName").asText(),120);
   }else text(e.path("chapter").asText(),120);
   if(!codes.add(e.path("code").asText())||!e.path("order").isIntegralNumber()||e.path("order").asInt()<1||!orders.add(e.path("order").asInt())||!e.path("required").isBoolean()||!e.path("minCount").isIntegralNumber()||e.path("minCount").asInt()<0||e.path("minCount").asInt()>1000||e.path("required").asBoolean()&&e.path("minCount").asInt()<1||!KINDS.contains(e.path("kind").asText())||!Set.of("BATCH","OPERATION").contains(e.path("scope").asText()))throw invalid();
   if(!e.path("fields").isArray()||e.path("fields").size()>200||!e.path("attachmentIds").isArray()||e.path("attachmentIds").size()>100)throw invalid();
   var fs=new HashSet<String>();for(var f:e.path("fields")){code(f.asText());if(!fs.add(f.asText()))throw invalid();}for(var a:e.path("attachmentIds"))MasterMutation.id(a.asText());
   if(e.path("kind").asText().equals("FORM")){
    code(e.path("formCode").asText());code(e.path("operationCode").asText());if(e.path("fields").isEmpty()||!forms.add(e.path("formCode").asText()))throw invalid();
    if(modern&&!e.path("flowType").asText().equals("PROCESS"))throw invalid();
   }else if(e.hasNonNull("formCode")||e.hasNonNull("printTemplateVersionId"))throw invalid();
   if(e.hasNonNull("printTemplateVersionId"))MasterMutation.id(e.path("printTemplateVersionId").asText());
   if(modern&&e.hasNonNull("archiveStage")&&!Set.of("PRODUCTION_REVIEW","QA_REVIEW","POST_RELEASE").contains(e.path("archiveStage").asText()))throw invalid();
   if(modern&&e.hasNonNull("applicability")&&e.path("applicability").asText().length()>200)throw invalid();
  }
  try{
   if(modern)return new BookDefinition(
    n.path("templateName").asText(),n.path("processPackageId").asText(),n.path("ebrTemplateVersionId").asText(),
    null,null,List.of(),parseEntries(n.path("entries"),json)
   );
   return new BookDefinition(
    null,null,null,n.path("varietyCode").asText(),n.path("varietyName").asText(),
    parseMappings(n.path("mappings"),json),parseEntries(n.path("entries"),json)
   );
  }catch(Exception ex){throw new IllegalArgumentException("Invalid closed book definition",ex);}
 }
 private static List<Mapping> parseMappings(JsonNode node,ObjectMapper json){var out=new ArrayList<Mapping>();node.forEach(x->out.add(json.convertValue(x,Mapping.class)));return List.copyOf(out);}
 private static List<Entry> parseEntries(JsonNode node,ObjectMapper json){var out=new ArrayList<Entry>();node.forEach(x->out.add(json.convertValue(x,Entry.class)));return List.copyOf(out);}
 private static void exact(JsonNode n,Set<String> keys){if(!n.isObject())throw invalid();n.fieldNames().forEachRemaining(k->{if(!keys.contains(k))throw new IllegalArgumentException("Unknown book field: "+k);});}
 private static void code(String s){if(s==null||!s.matches("[A-Za-z][A-Za-z0-9_-]{0,63}"))throw invalid();}
 private static void text(String s,int max){if(s==null||s.isBlank()||s.length()>max||s.chars().anyMatch(Character::isISOControl))throw invalid();}
 private static IllegalArgumentException invalid(){return new IllegalArgumentException("Invalid electronic batch record template, flow, record or scope");}
}
