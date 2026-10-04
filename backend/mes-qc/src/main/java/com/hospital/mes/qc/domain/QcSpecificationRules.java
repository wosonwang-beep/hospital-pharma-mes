package com.hospital.mes.qc.domain;
import com.hospital.mes.common.exception.*;
import java.math.BigDecimal;
import java.util.*;
public final class QcSpecificationRules {
 private QcSpecificationRules(){}
 public static String text(String s,int max){if(s==null||s.isBlank()||s.strip().length()>max)throw invalid("Nonblank text up to "+max+" characters required");return s.strip();}
 public static long id(String s){if(s==null||!s.matches("[1-9][0-9]*"))throw invalid("Positive decimal ID required");try{return Long.parseLong(s);}catch(NumberFormatException e){throw invalid("ID exceeds supported range");}}
 public static long expected(String header,Long body){if(header==null||!header.matches("\"(0|[1-9][0-9]*)\""))throw new ValidationException("INVALID_IF_MATCH","If-Match must be a quoted nonnegative version");try{long n=Long.parseLong(header.substring(1,header.length()-1));if(body==null||body!=n)throw invalid("Body version must match If-Match");return n;}catch(NumberFormatException e){throw invalid("Invalid version");}}
 public static BigDecimal decimal(String s){if(s==null)return null;if(!s.matches("-?[0-9]{1,12}(\\.[0-9]{1,6})?"))throw invalid("Decimal requires at most twelve integer and six fractional digits");return new BigDecimal(s).setScale(6);}
 public static void validateItems(List<QcCommands.Item> items){if(items==null||items.isEmpty())throw invalid("At least one item required");Set<String> seen=new HashSet<>();for(var i:items){if(i==null)throw invalid("Item required");String code=text(i.itemCode(),100);if(!seen.add(code.toUpperCase(Locale.ROOT)))throw invalid("Duplicate item code");text(i.itemName(),200);text(i.methodCode(),100);text(i.methodVersion(),50);if(i.required()==null)throw invalid("required flag required");var lo=decimal(i.lowerLimit());var hi=decimal(i.upperLimit());if("NUMERIC".equals(i.resultType())){if((lo==null&&hi==null)||i.unitId()==null||i.textAcceptanceCriteria()!=null)throw invalid("NUMERIC requires bounds/unit and no text criterion");id(i.unitId());if(lo!=null&&hi!=null&&lo.compareTo(hi)>0)throw invalid("Lower limit exceeds upper limit");}else if("TEXT".equals(i.resultType())){if(lo!=null||hi!=null||i.unitId()!=null)throw invalid("TEXT cannot contain numeric fields");text(i.textAcceptanceCriteria(),1000);}else throw invalid("Unknown result type");}}
 public static boolean numericPass(QcCommands.Item i,BigDecimal value){if(value==null||!"NUMERIC".equals(i.resultType()))return false;var lo=decimal(i.lowerLimit());var hi=decimal(i.upperLimit());return (lo!=null||hi!=null)&&(lo==null||value.compareTo(lo)>=0)&&(hi==null||value.compareTo(hi)<=0);}
 public static void requireDraft(String status){if(!"DRAFT".equals(status))throw conflict("QC_SPEC_STATE_INVALID","Only DRAFT content is editable");}
 public static String transition(String state,String action){if("DRAFT".equals(state)&&"APPROVE".equals(action))return "APPROVED";if("APPROVED".equals(state)&&"RETIRE".equals(action))return "RETIRED";throw conflict("QC_SPEC_STATE_INVALID","Invalid QC specification transition");}
 public static ValidationException invalid(String message){return new ValidationException("VALIDATION_ERROR",message);}
 public static ResourceConflictException conflict(String code,String message){return new ResourceConflictException(code,message);}
}
