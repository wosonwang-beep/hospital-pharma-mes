package com.hospital.mes.execution.domain;
import com.hospital.mes.common.exception.*;
public final class ExecutionRules {
 private ExecutionRules(){}
 public static String start(String state){require(state,"READY");return "IN_PROGRESS";}
 public static String pause(String state){require(state,"IN_PROGRESS");return "PAUSED";}
 public static String resume(String state){require(state,"PAUSED");return "IN_PROGRESS";}
 public static String complete(String state){require(state,"IN_PROGRESS");return "COMPLETED";}
 public static void independent(long recorder,long verifier,boolean required){if(required&&recorder==verifier)throw new ComplianceException("SELF_VERIFICATION_FORBIDDEN","Independent verifier required");}
 private static void require(String actual,String wanted){if(!wanted.equals(actual))throw new StateTransitionException("INVALID_OPERATION_STATE","Operation must be "+wanted);}
}
