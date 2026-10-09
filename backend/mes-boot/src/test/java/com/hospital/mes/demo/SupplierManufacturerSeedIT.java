package com.hospital.mes.demo;

import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.masterdata.application.MaterialSupplierService;
import com.hospital.mes.masterdata.application.SupplierCommands;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
@Commit
class SupplierManufacturerSeedIT {
 @Autowired MaterialSupplierService links;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean CurrentPlatformContextResolver contexts;

 @Test void addManufacturerToExistingSupplierRelationships(){
  long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized='admin'",Long.class);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("SYSTEM_ADMIN"),
      Set.of("master:material:view","master:material:update"),"manufacturer-seed","manufacturer-seed"));
  for(String materialId:jdbc.queryForList("SELECT DISTINCT material_id FROM md_material_supplier WHERE org_id=1",String.class)){
   var current=links.get(materialId);
   var wanted=new ArrayList<SupplierCommands.Relationship>();
   for(var row:current.path("suppliers")){
    String supplierId=row.path("supplierId").asText();
    String name=row.path("supplierName").asText();
    LocalDate validTo=row.hasNonNull("validTo")?LocalDate.parse(row.path("validTo").asText()):null;
    wanted.add(new SupplierCommands.Relationship(
      supplierId,row.path("approved").asBoolean(),row.path("preferred").asBoolean(),validTo,name));
   }
   links.assign(materialId,new SupplierCommands.Assign(current.path("versionNo").asLong(),
       "补充实际供货关系中的生产厂家信息，用于来料追溯与集成测试",wanted),null,UUID.randomUUID().toString());
  }
 }
}
