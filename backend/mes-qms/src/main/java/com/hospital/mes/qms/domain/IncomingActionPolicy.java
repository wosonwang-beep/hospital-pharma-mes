package com.hospital.mes.qms.domain;
import java.util.*;
import com.hospital.mes.common.exception.ResourceConflictException;
/** State candidates for existing commands only. Application evidence narrows availability. */
public final class IncomingActionPolicy {
 private IncomingActionPolicy(){}
 public static List<String> candidates(String table,String status,String sampleType){
  if(status==null)return List.of();
  return switch(table){
   case "qms_inspection_request" -> switch(status){case "DRAFT"->List.of("submit");case "SUBMITTED"->List.of("accept");default->List.of();};
   case "qms_sampling_task" -> switch(status){case "PLANNED"->List.of("approve-plan","assign");case "ASSIGNED"->List.of("start");case "IN_PROGRESS"->List.of("details","complete");default->List.of();};
   case "qms_sample" -> {var out=new ArrayList<String>();out.add("label");if(status.equals("COLLECTED"))out.add("receive");if(status.equals("TEST_COMPLETED")||status.equals("RECEIVED")&&"RETENTION_SAMPLE".equals(sampleType))out.add("retain");if(Set.of("TEST_COMPLETED","RETAINED").contains(status))out.add("dispose");yield List.copyOf(out);}
   case "qms_inspection_task" -> switch(status){case "CREATED"->List.of("assign");case "ASSIGNED"->List.of("start");case "IN_PROGRESS"->List.of("submit-review");case "PENDING_REVIEW"->List.of("review");default->List.of();};
   case "qms_inspection_report" -> switch(status){case "DRAFT"->List.of("review");case "REVIEWED"->List.of("approve");default->List.of();};
   case "qms_deviation" -> switch(status){case "OPEN"->List.of("update","investigate");case "INVESTIGATING"->List.of("update","decide");case "DECIDED"->List.of("close");default->List.of();};
   default -> List.of();
  };
 }
 public static void require(String table,String status,String type,String action){if(!candidates(table,status,type).contains(action))throw new ResourceConflictException("STATE_CONFLICT","Command not allowed in "+status);}
}
