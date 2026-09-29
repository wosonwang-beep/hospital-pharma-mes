package com.hospital.mes.integration.application;
import com.hospital.mes.integration.domain.IntegrationDirection;import java.time.Instant;import java.util.List;
public record IntegrationMessageQuery(IntegrationDirection direction,String system,String messageId,String eventType,
 String aggregateType,String aggregateId,List<String> status,Instant occurredFrom,Instant occurredTo,int page,int size){
 public IntegrationMessageQuery{status=status==null?List.of():List.copyOf(status);if(page<0||size<1||size>200)throw new IllegalArgumentException("invalid page");if(occurredFrom!=null&&occurredTo!=null&&!occurredFrom.isBefore(occurredTo))throw new IllegalArgumentException("invalid time range");}
}
