package com.hospital.mes.wms.domain;
import com.hospital.mes.common.exception.*;
import java.math.BigDecimal;
import java.util.List;
public final class MaterialRequestRules {
 private MaterialRequestRules(){}
 public static void editable(String status){if(!"DRAFT".equals(status))throw new StateTransitionException("INVALID_REQUEST_STATE","Request must be DRAFT");}
 public static void issuable(String status){if(!List.of("SUBMITTED","PARTIALLY_ISSUED").contains(status))throw new StateTransitionException("INVALID_REQUEST_STATE","Request must be submitted and unfulfilled");}
 public static void cancellable(String status,boolean linked){if(!List.of("DRAFT","SUBMITTED").contains(status))throw new StateTransitionException("INVALID_REQUEST_STATE","Request cannot cancel in this state");if(linked)throw new ComplianceException("REQUEST_HAS_ISSUES","Request has linked issues");}
 public static String fulfillment(List<BigDecimal> requested,List<BigDecimal> issued){
  if(requested.isEmpty()||requested.size()!=issued.size())throw new IllegalArgumentException("Request lines required");boolean any=false,full=true;
  for(int i=0;i<requested.size();i++){var demand=requested.get(i);var total=issued.get(i);if(demand.signum()<=0||total.signum()<0)throw new IllegalArgumentException("Invalid request quantities");if(total.compareTo(demand)>0)throw new ComplianceException("REQUEST_QTY_EXCEEDED","REQUEST_QTY_EXCEEDED: confirmed issue exceeds requested quantity");any|=total.signum()>0;full&=total.compareTo(demand)==0;}
  return full?"FULFILLED":any?"PARTIALLY_ISSUED":"SUBMITTED";
 }
 public static BigDecimal available(BigDecimal onHand,BigDecimal reserved){var result=onHand.subtract(reserved);if(onHand.signum()<0||reserved.signum()<0||result.signum()<0)throw new ComplianceException("INVENTORY_INTEGRITY","INVENTORY_INTEGRITY: inconsistent outstanding reservation balance");return result;}
}
