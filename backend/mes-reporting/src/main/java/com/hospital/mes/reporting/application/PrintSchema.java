package com.hospital.mes.reporting.application;
import com.hospital.mes.reporting.domain.PrintType;
import java.util.*;
/** LIST uses registered record-header fields; DOCUMENT retains the original provider contract. */
public record PrintSchema(List<PrintField> definitions,Set<String> fields,Set<String> itemFields,Map<String,Object> example) {
 public static PrintSchema of(PrintDataProvider p,PrintType type){return of(p,type.name());}
 public static String recordKey(String key){return "record"+Character.toUpperCase(key.charAt(0))+key.substring(1);}
 public static PrintSchema of(PrintDataProvider p,String type){
  if(PrintType.parse(type)==PrintType.DOCUMENT)return new PrintSchema(p.fieldDefinitions(),p.fields(),p.itemFields(),p.example());
  var definitions=new ArrayList<PrintField>();
  definitions.add(new PrintField("reportNo","列表编号","打印信息","当前列表输出编号","LIST-EXAMPLE",false));
  definitions.add(new PrintField("modeLabel","打印标识","打印信息","列表输出，不替代正式单据","列表输出",false));
  definitions.add(new PrintField("recordCount","记录数量","打印信息","当前选中记录数","2",false));
  var keys=new LinkedHashSet<String>();
  for(var field:p.fieldDefinitions())if(!field.repeated()&&!field.key().equals("items")){String key=recordKey(field.key());keys.add(key);definitions.add(new PrintField(key,field.label(),"选中记录",field.description(),field.example(),true,field.dataType(),field.unitField()==null?null:recordKey(field.unitField())));}
  return new PrintSchema(List.copyOf(definitions),Set.of("reportNo","modeLabel","recordCount","items"),Set.copyOf(keys),listData(p,List.of(p.example(),p.example()),"LIST-EXAMPLE"));
 }
 public static Map<String,Object> listData(PrintDataProvider p,List<Map<String,Object>> records,String number){
  var rows=new ArrayList<Map<String,Object>>();for(var record:records){var row=new LinkedHashMap<String,Object>();for(var field:p.fieldDefinitions())if(!field.repeated()&&!field.key().equals("items"))row.put(recordKey(field.key()),record.getOrDefault(field.key(),""));rows.add(row);}
  return Map.of("reportNo",number,"modeLabel","列表输出","recordCount",records.size(),"items",List.copyOf(rows));
 }
}
