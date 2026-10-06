package com.hospital.mes.traceability.application;
import com.fasterxml.jackson.databind.JsonNode;
public interface WmsTracePort {JsonNode forLot(long organizationId,long materialLotId);}
