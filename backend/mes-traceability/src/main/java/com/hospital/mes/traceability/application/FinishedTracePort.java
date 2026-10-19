package com.hospital.mes.traceability.application;
import com.fasterxml.jackson.databind.JsonNode;
public interface FinishedTracePort {JsonNode forLot(long organizationId,long materialLotId);Long lotForBatch(long organizationId,long batch);default JsonNode forBatch(long organizationId,long batch){Long lot=lotForBatch(organizationId,batch);return lot==null?null:forLot(organizationId,lot);}}
