package com.hospital.mes.demo;

import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.masterdata.application.SupplierCommands;
import com.hospital.mes.masterdata.application.SupplierService;
import java.time.LocalDate;
import java.util.Map;
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
class SupplierValiditySeedIT {
    @Autowired SupplierService suppliers;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CurrentPlatformContextResolver contexts;

    @Test
    void addRealisticQualificationValidityDates() {
        long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized='admin'",Long.class);
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1,actor,Set.of("SYSTEM_ADMIN"),Set.of("master:supplier:view","master:supplier:update"),
            "supplier-validity-seed","supplier-validity-seed"));
        Map<String,LocalDate> dates=Map.of(
            "1",LocalDate.of(2027,12,31),
            "3",LocalDate.of(2027,9,30),
            "4",LocalDate.of(2027,11,30),
            "5",LocalDate.of(2027,8,31),
            "6",LocalDate.of(2027,10,31),
            "7",LocalDate.of(2028,12,31)
        );
        dates.forEach((id,date)->{
            var current=suppliers.get(id);
            if(date.toString().equals(current.path("validTo").asText()))return;
            suppliers.command(id,new SupplierCommands.Update(
                "UPDATE",current.path("versionNo").asLong(),
                "补录年度供应商资格复审有效期，用于集成测试与页面验收",
                current.path("supplierName").asText(),date),null,UUID.randomUUID().toString());
        });
    }
}
