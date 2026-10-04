package com.hospital.mes.masterdata.application;
/** Returns detached basic snapshots. Future consumers persist these at use time. */
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialQueryService {
 private final MaterialService materials;
 public MaterialQueryService(MaterialService materials){this.materials=materials;}
 public com.fasterxml.jackson.databind.JsonNode snapshot(long org,long materialId){return materials.snapshot(org,materialId);}
 public com.fasterxml.jackson.databind.JsonNode requireUsable(long org,long materialId,java.time.Instant at){return materials.requireUsable(org,materialId,at);}
}
