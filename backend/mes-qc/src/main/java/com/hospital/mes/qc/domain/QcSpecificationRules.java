package com.hospital.mes.qc.domain;
import com.hospital.mes.common.exception.*;
import java.math.BigDecimal;
import java.util.*;
/**
 * Fail-closed value rules of the QC quality standard basic library. There is no draft/approve/retire
 * state: a standard is either ACTIVE (usable by downstream inspection) or INACTIVE (kept as history).
 */
public final class QcSpecificationRules {
 private QcSpecificationRules(){}
 public static final String ACTIVE="ACTIVE";
 public static final String INACTIVE="INACTIVE";
 public static final List<String> BASIS_TYPES=List.of("CHINESE_PHARMACOPOEIA","MINISTRY_STANDARD","DRUG_REGISTRATION_STANDARD","NATIONAL_DRUG_STANDARD","ENTERPRISE_INTERNAL","CUSTOMER_SPECIFICATION","OTHER");
 public static final List<String> SUBJECT_TYPES=List.of("MATERIAL","PRODUCT");
 public static String text(String s,int max){if(s==null||s.isBlank()||s.strip().length()>max)throw invalid("Nonblank text up to "+max+" characters required");return s.strip();}
 public static String optionalText(String s,int max){if(s==null||s.isBlank())return null;if(s.strip().length()>max)throw invalid("Text up to "+max+" characters allowed");return s.strip();}
 public static long id(String s){if(s==null||!s.matches("[1-9][0-9]*"))throw invalid("Positive decimal ID required");try{return Long.parseLong(s);}catch(NumberFormatException e){throw invalid("ID exceeds supported range");}}
 public static List<Long> ids(List<String> values,String label,int max){if(values==null)return List.of();if(values.size()>max)throw invalid(label+" exceeds "+max+" entries");LinkedHashSet<Long> out=new LinkedHashSet<>();for(String v:values)out.add(id(v));return List.copyOf(out);}
 public static long expected(String header,Long body){if(header==null||!header.matches("\"(0|[1-9][0-9]*)\""))throw new ValidationException("INVALID_IF_MATCH","If-Match must be a quoted nonnegative version");try{long n=Long.parseLong(header.substring(1,header.length()-1));if(body==null||body!=n)throw invalid("Body recordVersion must match If-Match");return n;}catch(NumberFormatException e){throw invalid("Invalid version");}}
 public static BigDecimal decimal(String s){if(s==null||s.isBlank())return null;String t=s.strip();if(!t.matches("-?[0-9]{1,12}(\\.[0-9]{1,6})?"))throw invalid("Decimal requires at most twelve integer and six fractional digits");return new BigDecimal(t).setScale(6);}
 public static void validateItems(List<QcCommands.Item> items){if(items==null||items.isEmpty())throw invalid("At least one item required");Set<String> seen=new HashSet<>();for(var i:items){if(i==null)throw invalid("Item required");String code=text(i.itemCode(),100);if(!seen.add(code.toUpperCase(Locale.ROOT)))throw invalid("Duplicate item code");text(i.itemName(),200);text(i.methodCode(),100);text(i.methodVersion(),50);if(i.required()==null)throw invalid("required flag required");var lo=decimal(i.lowerLimit());var hi=decimal(i.upperLimit());if("NUMERIC".equals(i.resultType())){if((lo==null&&hi==null)||i.unitId()==null||i.textAcceptanceCriteria()!=null)throw invalid("NUMERIC requires bounds/unit and no text criterion");id(i.unitId());if(lo!=null&&hi!=null&&lo.compareTo(hi)>0)throw invalid("Lower limit exceeds upper limit");}else if("TEXT".equals(i.resultType())){if(lo!=null||hi!=null||i.unitId()!=null)throw invalid("TEXT cannot contain numeric fields");text(i.textAcceptanceCriteria(),1000);}else throw invalid("Unknown result type");}}
 public static boolean numericPass(QcCommands.Item i,BigDecimal value){if(value==null||!"NUMERIC".equals(i.resultType()))return false;var lo=decimal(i.lowerLimit());var hi=decimal(i.upperLimit());return (lo!=null||hi!=null)&&(lo==null||value.compareTo(lo)>=0)&&(hi==null||value.compareTo(hi)<=0);}
 public static String status(String s){if(s==null)return null;String t=s.strip();if(!ACTIVE.equals(t)&&!INACTIVE.equals(t))throw invalid("Unknown standard status");return t;}
 public static String basisType(String s){String t=text(s,32);if(!BASIS_TYPES.contains(t))throw invalid("Unknown standard basis type");return t;}
 public static ValidationException invalid(String message){return new ValidationException("VALIDATION_ERROR",message);}
 public static ResourceConflictException conflict(String code,String message){return new ResourceConflictException(code,message);}
}
