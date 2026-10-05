package com.hospital.mes.production.domain;
import java.math.BigDecimal;
import com.hospital.mes.common.exception.*;
public final class ProductionRules {
 private ProductionRules(){}
 public static void allocation(BigDecimal planned,BigDecimal allocated){gate(allocated.signum()>=0&&allocated.compareTo(planned)<=0,"ORDER_ALLOCATION_EXCEEDED","Batch allocation exceeds order plan");}
 public static void requireDraft(String state){if(!"DRAFT".equals(state))throw new StateTransitionException("INVALID_STATE","Only draft records can be edited");}
 public static void gate(boolean allowed,String code,String message){if(!allowed)throw new ComplianceException(code,message);}
 public static void version(long actual,long expected){if(actual!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Reload the current record");}
 public static void weighing(BigDecimal target,BigDecimal actual,BigDecimal tolerance,BigDecimal precision){
  gate(target!=null&&actual!=null&&target.signum()>0&&actual.signum()>0,"CHARGE_QTY_INVALID","Positive weighing values required");
  gate(tolerance!=null&&precision!=null&&tolerance.signum()>=0&&precision.signum()>0,"WEIGHING_POLICY_REQUIRED","Frozen weighing tolerance and precision required");
  gate(actual.remainder(precision).signum()==0,"WEIGHING_PRECISION_INVALID","Value does not match weighing precision");
  gate(actual.subtract(target).abs().multiply(new BigDecimal("100")).compareTo(target.multiply(tolerance))<=0,"WEIGHING_OUT_OF_TOLERANCE","Weighing exceeds frozen tolerance");
 }
 public static String release(String state){requireDraft(state);return "RELEASED";}
 public static String startBatch(String state){if(!"RELEASED".equals(state))throw new StateTransitionException("INVALID_BATCH_STATE","Batch is not released");return "IN_PROGRESS";}
 public static String finishedQaDecision(String state,String decision,boolean superseding){gate(java.util.Set.of("RELEASED","REJECTED").contains(decision),"FINISHED_DECISION_INVALID","Controlled QA decision required");gate(superseding?java.util.Set.of("QA_RELEASED","REJECTED").contains(state):"PENDING_QA".equals(state),"FINISHED_RELEASE_STATE_CONFLICT","Pending QA or explicit predecessor required");return decision.equals("RELEASED")?"QA_RELEASED":"REJECTED";}
}
