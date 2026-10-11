package com.hospital.mes.reporting.application;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

/** Template-bound provider adapter; new source publication never changes this fixed revision. */
public final class ConfiguredPrintProvider implements PrintDataProvider {
 private final PrintDataSourceService service;private final PrintDataSourceService.FixedSource source;private final String type;private final List<PrintField> definitions;
 public ConfiguredPrintProvider(PrintDataSourceService service,PrintDataSourceService.FixedSource source,String type){this.service=service;this.source=source;this.type=type;var fields=new ArrayList<PrintField>();for(var f:service.configuration(source).path("fields"))if(f.path("available").asBoolean()){boolean repeated=f.hasNonNull("datasetKey");String example=switch(f.path("dataType").asText()){case "NUMBER"->"0";case "BOOLEAN"->"false";case "DATE"->"2026-01-01";case "DATETIME"->"2026-01-01T00:00:00Z";default->"合成字段示例";};fields.add(new PrintField(f.path("fieldKey").asText(),f.path("label").asText(),repeated?datasetLabel(f.path("datasetKey").asText()):"主信息","固定源修订字段；模板排版预览使用合成示例",example,repeated,f.path("dataType").asText(),null,repeated?f.path("datasetKey").asText():null));}if(fields.stream().noneMatch(f->f.key().equals("modeLabel")))fields.add(new PrintField("modeLabel","打印标识","打印信息","业务资格标识","合成预览 · 非业务记录",false));if(fields.stream().noneMatch(f->f.key().equals("reportNo")))fields.add(new PrintField("reportNo","单据编号","打印信息","真实单据身份","SYNTHETIC-PREVIEW",false));definitions=List.copyOf(fields);}
 private String datasetLabel(String key){for(var j:service.configuration(source).path("joins"))if(j.path("datasetKey").asText().equals(key))return j.path("label").asText();return key;}
 public String businessType(){return type;}
 public List<PrintField> fieldDefinitions(){return definitions;}
 public Set<String> fields(){var out=new LinkedHashSet<String>();for(var f:definitions)if(!f.repeated())out.add(f.key());out.addAll(datasets().keySet());return Set.copyOf(out);}
 public Map<String,Set<String>> datasets(){var out=new LinkedHashMap<String,Set<String>>();for(var f:definitions)if(f.repeated())out.computeIfAbsent(f.datasetKey(),k->new LinkedHashSet<>()).add(f.key());return Collections.unmodifiableMap(out);}
 public Set<String> itemFields(){return datasets().getOrDefault("items",Set.of());}
 public Map<String,Object> example(){var result=new LinkedHashMap<String,Object>();for(var f:definitions)if(!f.repeated())result.put(f.key(),f.example());for(var dataset:datasets().entrySet()){var row=new LinkedHashMap<String,Object>();for(var key:dataset.getValue())row.put(key,definitions.stream().filter(f->f.key().equals(key)).findFirst().orElseThrow().example());result.put(dataset.getKey(),List.of(row));}return Collections.unmodifiableMap(result);}
 public void authorizeRead(String id){service.authorize(source,type,id);}
 public PrintSnapshot load(String id,boolean formal){service.authorize(source,type,id);return service.resolve(source,id,formal).snapshot();}
}
