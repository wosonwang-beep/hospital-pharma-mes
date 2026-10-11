package com.hospital.mes.reporting.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.reporting.domain.PrintSourceConfiguration;
import com.hospital.mes.common.exception.ComplianceException;
import java.util.*;
import org.springframework.stereotype.Component;

/** One independent query path per collection; no SQL joining two repeated branches. */
@Component
public class ConfiguredPrintResolver {
 private final PrintSourceCatalog catalog;
 public ConfiguredPrintResolver(PrintSourceCatalog catalog){this.catalog=catalog;}
 public record Result(PrintDataProvider.PrintSnapshot snapshot,Map<String,Object> rawData){}
 public Result resolve(long org,JsonNode configuration,JsonNode metadata,String id,boolean formal){
  var config=PrintSourceConfiguration.normalize(configuration);catalog.verify(config);String rootId=config.path("mainCatalogId").asText();String ref=metadata.path("authorizationPolicyRef").asText();if(!catalog.compatible(ref,rootId))throw new ComplianceException("PRINT_SOURCE_POLICY_INCOMPATIBLE","策略与主表不兼容");
  long deadline=System.nanoTime()+5_000_000_000L;var policy=catalog.policy(ref);policy.authorizeRead(id);var snapshot=policy.load(id,formal);
  var run=new Run(org,config,metadata.path("fixedTypeFilters"),snapshot,id,deadline);var root=catalog.entity(rootId);var rows=run.rows(root,Map.of("id",id),com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode());if(rows.isEmpty())throw new NoSuchElementException("Record not found or fixed filter excluded it");if(rows.size()!=1)throw new ComplianceException("PRINT_SOURCE_CARDINALITY_INVALID","主记录数量必须为1");
  var context=new HashMap<String,Map<String,Object>>();context.put("main",rows.getFirst());run.singles("main",context);var display=run.project(null,context,false);var raw=run.project(null,context,true);run.datasets("main",context,display,raw);
  if(System.nanoTime()>deadline)throw new ComplianceException("PRINT_SOURCE_LIMIT_EXCEEDED","取数超时");
  // Controlled print watermark/report identity continue to come from the qualified business provider.
  display.put("modeLabel",snapshot.data().getOrDefault("modeLabel",formal?"正式记录":"草稿记录"));display.put("reportNo",snapshot.reportNo());raw.put("modeLabel",display.get("modeLabel"));raw.put("reportNo",snapshot.reportNo());
  return new Result(new PrintDataProvider.PrintSnapshot(snapshot.businessVersion(),snapshot.reportNo(),display),Collections.unmodifiableMap(raw));
 }
 private final class Run {
  final long org,deadline;final JsonNode config,filters;final PrintDataProvider.PrintSnapshot snapshot;final String id;final Map<String,List<JsonNode>> children=new HashMap<>();
  Run(long org,JsonNode config,JsonNode filters,PrintDataProvider.PrintSnapshot snapshot,String id,long deadline){this.org=org;this.config=config;this.filters=filters;this.snapshot=snapshot;this.id=id;this.deadline=deadline;for(var j:config.path("joins"))children.computeIfAbsent(j.path("parentNodeId").asText(),k->new ArrayList<>()).add(j);}
  List<Map<String,Object>> rows(PrintSourceCatalog.Entity e,Map<String,Object> keys,JsonNode sort){
   if(System.nanoTime()>deadline)throw new ComplianceException("PRINT_SOURCE_LIMIT_EXCEEDED","取数超时");if(e.table()!=null)return catalog.query(e,org,keys,filters,sort,deadline);
   var result=new ArrayList<Map<String,Object>>();if(e.providerDataset()==null){var row=new LinkedHashMap<String,Object>(snapshot.data());row.put("id",id);if(catalog.matches(e,row,filters))result.add(row);}else{
    Object collection=snapshot.data().get(e.providerDataset());if(collection instanceof List<?> list)for(var item:list){if(!(item instanceof Map<?,?> values))throw new ComplianceException("PRINT_SOURCE_STRUCTURE_INVALID","业务集合结构无效");var row=new LinkedHashMap<String,Object>();values.forEach((k,v)->row.put(k.toString(),v));if(catalog.matches(e,row,filters))result.add(row);}}
   if(result.size()>2000)throw new ComplianceException("PRINT_SOURCE_LIMIT_EXCEEDED","集合超过2000行");return result;
  }
  List<Map<String,Object>> related(JsonNode j,Map<String,Map<String,Object>> context){var parent=context.get(j.path("parentNodeId").asText());if(parent==null||parent.isEmpty())return List.of();var keys=new LinkedHashMap<String,Object>();for(var key:j.path("keys"))keys.put(key.path("targetFieldId").asText(),parent.get(key.path("parentFieldId").asText()));var e=catalog.entity(j.path("catalogId").asText());var rows=rows(e,keys,j.path("sort"));if(rows.size()>2000)throw new ComplianceException("PRINT_SOURCE_LIMIT_EXCEEDED","集合超过2000行");if(!j.path("cardinality").asText().equals("1:N")&&rows.size()>1)throw new ComplianceException("PRINT_SOURCE_CARDINALITY_INVALID","单值关联返回多行");return rows;}
  void singles(String node,Map<String,Map<String,Object>> context){for(var j:children.getOrDefault(node,List.of()))if(!j.path("cardinality").asText().equals("1:N")){var rows=related(j,context);String target=j.path("nodeId").asText();context.put(target,rows.isEmpty()?Map.of():rows.getFirst());singles(target,context);}}
  Map<String,Object> project(String dataset,Map<String,Map<String,Object>> context,boolean raw){var result=new LinkedHashMap<String,Object>();for(var f:config.path("fields"))if(f.path("available").asBoolean()&&Objects.equals(dataset,f.hasNonNull("datasetKey")?f.path("datasetKey").asText():null)){var source=f.path("sourcePath");var row=context.getOrDefault(source.path("nodeId").asText(),Map.of());Object value=row.get(source.path("catalogFieldId").asText());result.put(f.path("fieldKey").asText(),raw?PrintSourceConfiguration.raw(value,f.path("dataType").asText()):PrintSourceConfiguration.display(value,f));}return result;}
  void datasets(String node,Map<String,Map<String,Object>> context,Map<String,Object> display,Map<String,Object> raw){for(var j:children.getOrDefault(node,List.of())){String target=j.path("nodeId").asText();if(j.path("cardinality").asText().equals("1:N")){var displayed=new ArrayList<Map<String,Object>>();var original=new ArrayList<Map<String,Object>>();String key=j.path("datasetKey").asText();for(var row:related(j,context)){var branch=new HashMap<>(context);branch.put(target,row);singles(target,branch);displayed.add(project(key,branch,false));original.add(project(key,branch,true));}display.put(key,List.copyOf(displayed));raw.put(key,List.copyOf(original));}else datasets(target,context,display,raw);}}
 }
}

