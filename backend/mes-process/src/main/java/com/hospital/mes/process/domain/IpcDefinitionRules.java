package com.hospital.mes.process.domain;
import com.hospital.mes.process.domain.ProcessCommands.IpcDefinition;
import java.math.BigDecimal;
import java.util.*;
import static com.hospital.mes.process.domain.ProcessRules.*;
public final class IpcDefinitionRules {
 private IpcDefinitionRules(){}
 private static BigDecimal limit(String raw){gate(raw!=null&&raw.matches("-?\\d+(\\.\\d{1,8})?"),"IPC limit requires DECIMAL(24,8) string");var n=new BigDecimal(raw);gate(n.precision()-n.scale()<=16,"IPC limit precision exceeded");return n;}
 public static void validate(List<IpcDefinition> definitions){gate(definitions!=null&&definitions.size()<=200,"IPC definitions max 200");var codes=new HashSet<String>();for(var d:definitions){gate(d!=null,"IPC definition required");text(d.ipcCode(),64,"ipcCode");text(d.name(),200,"IPC name");text(d.methodReference(),200,"IPC methodReference");gate(codes.add(d.ipcCode()),"Duplicate IPC code");gate(d.required()!=null,"IPC required flag missing");gate(Set.of("NUMERIC","TEXT").contains(d.resultType()==null?"":d.resultType()),"Invalid IPC result type");if("NUMERIC".equals(d.resultType())){gate(d.lowerLimit()!=null||d.upperLimit()!=null,"IPC numeric bound required");gate(d.expectedText()==null,"Numeric IPC cannot have text expectation");var lo=d.lowerLimit()==null?null:limit(d.lowerLimit());var hi=d.upperLimit()==null?null:limit(d.upperLimit());gate(lo==null||hi==null||lo.compareTo(hi)<=0,"IPC limits reversed");}else{ text(d.expectedText(),1000,"IPC expectedText");gate(d.lowerLimit()==null&&d.upperLimit()==null&&d.unitId()==null,"Text IPC cannot have numeric limits/unit");}}}
}
