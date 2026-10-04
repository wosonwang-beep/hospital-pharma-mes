package com.hospital.mes.masterdata.application;
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ApprovedSupplierQueryService {private final MaterialSupplierService suppliers;public ApprovedSupplierQueryService(MaterialSupplierService suppliers){this.suppliers=suppliers;}public com.fasterxml.jackson.databind.JsonNode requireApproved(long org,long materialId,long supplierId,java.time.Instant at){return suppliers.requireApproved(org,materialId,supplierId,at);}public com.fasterxml.jackson.databind.JsonNode requirePreferred(long org,long materialId,java.time.Instant at){return suppliers.requirePreferred(org,materialId,at);}
}
