package com.hospital.mes.release.domain;
import com.hospital.mes.common.exception.*;
import java.util.*;
public final class FinishedReleaseRules {
 private FinishedReleaseRules(){}
 public static void requireDecision(String decision){if(!Set.of("RELEASED","REJECTED").contains(decision))throw new IllegalArgumentException("Invalid finished decision");}
 public static String resultingBatchStatus(String current,String decision,boolean superseding){requireDecision(decision);if(!(superseding?Set.of("QA_RELEASED","REJECTED").contains(current):"PENDING_QA".equals(current)))throw new ResourceConflictException("FINISHED_RELEASE_STATE_CONFLICT","QA decision requires pending QA or explicit effective predecessor");return decision.equals("RELEASED")?"QA_RELEASED":"REJECTED";}
 public static void predecessor(Long effective,Long requested){if(!Objects.equals(effective,requested))throw new ResourceConflictException("RELEASE_SUPERSESSION_CONFLICT","Exact current decision predecessor required");}
 public static void digest(String current,String requested){if(!Objects.equals(current,requested))throw new ResourceConflictException("QA_REVIEW_STALE","Current evidence differs from the displayed QA review");}
}
