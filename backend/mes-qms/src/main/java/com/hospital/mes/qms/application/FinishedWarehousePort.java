package com.hospital.mes.qms.application;
import com.fasterxml.jackson.databind.JsonNode;
public interface FinishedWarehousePort {
 JsonNode confirmed(long org,long batch);
 JsonNode inbound(long org,long id);
}
