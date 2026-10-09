package com.hospital.mes.masterdata.infrastructure;
@org.springframework.stereotype.Repository @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SupplierStore extends com.hospital.mes.masterdata.application.ScopedStore<SupplierEntity> {
 public SupplierStore(SupplierMapper mapper){super(false,mapper,java.util.List.of("supplier_code","supplier_name"),java.util.Map.of("qualificationStatus","qualification_status"));}

}
