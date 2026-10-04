package com.hospital.mes.qms.domain;
import java.math.BigDecimal;
import com.hospital.mes.common.exception.*;
public final class IncomingRules {
 private IncomingRules(){}
 public static void gate(boolean condition,String code,String message){if(!condition)throw new ComplianceException(code,message);}
 public static void state(String actual,String...allowed){if(!java.util.Arrays.asList(allowed).contains(actual))throw new ResourceConflictException("STATE_CONFLICT","Command not allowed in "+actual);}
 public static void version(Long actual,long expected){if(actual==null||actual!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Record changed; reload");}
 public static BigDecimal quantity(String raw){BigDecimal q;try{q=new BigDecimal(raw);}catch(Exception e){throw new IllegalArgumentException("Invalid quantity");}gate(q.signum()>0&&q.scale()<=8&&q.precision()-q.scale()<=16,"INVALID_QUANTITY","Positive decimal(24,8) required");return q;}
 public static String numeric(BigDecimal v,BigDecimal low,BigDecimal high){gate(v!=null&&(low!=null||high!=null),"RESULT_REQUIRED","Numeric result and acceptance limits required");return (low!=null&&v.compareTo(low)<0)||(high!=null&&v.compareTo(high)>0)?"FAIL":"PASS";}
 public static void correction(String before,String after){gate(!"INVALID".equals(after)&&(!"FAIL".equals(before)||"FAIL".equals(after)),"RESULT_CORRECTION_FORBIDDEN","Original FAIL requires investigation; normal corrections cannot invalidate or pass it");}
 public static boolean resolvedFail(String status,String originalDisposition,String selectedConclusion,boolean signed){return "CLOSED".equals(status)&&"INVALID".equals(originalDisposition)&&"PASS".equals(selectedConclusion)&&signed;}
}
