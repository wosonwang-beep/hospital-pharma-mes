package com.hospital.mes.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.masterdata.application.QualificationCommands;
import com.hospital.mes.masterdata.application.QualificationService;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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

/**
 * Explicit, local DEV-only personnel demonstration qualification seed.
 * Real training/qualification evidence and approved roles are separate.
 * No production batch, GMP signature or release decision is touched.
 */
@SpringBootTest
@ActiveProfiles("ci")
@Transactional
@Commit
class DemoRoleQualificationSeedIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired QualificationService qualifications;
    @MockitoBean CurrentPlatformContextResolver contexts;

    record Grant(String login,String code) {}

    @Test
    void installDistinctLocalDemonstrationQualifications() {
        long administrator = jdbc.queryForObject(
            "SELECT id FROM sys_user WHERE login_name_normalized='admin'", Long.class);
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1,administrator,Set.of("SYSTEM_ADMIN"),
            Set.of("master:qualification:create","master:qualification:view"),
            "local-demo-qualification", "local-demo-qualification"));

        var grants=List.of(
            new Grant("demo.sampler","MES_DEMO_SAMPLING"),
            new Grant("demo.qc.analyst","MES_DEMO_QC_EXECUTION"),
            new Grant("demo.qc.reviewer","MES_DEMO_QC_REVIEW"),
            new Grant("demo.qa","MES_DEMO_QA_RELEASE"),
            new Grant("demo.production","MES_DEMO_PRODUCTION"),
            new Grant("demo.qc.reviewer","MES_DEMO_WEIGH_REVIEW")
        );
        LocalDate today=LocalDate.now(ZoneOffset.UTC);
        int created=0;
        for(var grant:grants) {
            var userIds=jdbc.queryForList(
                "SELECT id FROM sys_user WHERE login_name_normalized=? AND enabled=TRUE",
                Long.class,grant.login());
            assertThat(userIds).as("Existing enabled test identity: "+grant.login()).hasSize(1);
            String userId=Long.toString(userIds.getFirst());
            var current=jdbc.queryForList(
                "SELECT id FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code=?",
                Long.class, userIds.getFirst(),grant.code());
            if(current.isEmpty()) {
                qualifications.create(new QualificationCommands.Create(
                    userId,grant.code(),today.minusDays(30),today.plusYears(1)),
                    UUID.randomUUID().toString());
                created++;
            }
            Integer valid=jdbc.queryForObject(
                "SELECT COUNT(*) FROM md_qualification WHERE org_id=1 AND user_id=? AND qualification_code=? AND status='ACTIVE' AND valid_from<=? AND valid_to>=?",
                Integer.class,userIds.getFirst(),grant.code(),today,today);
            assertThat(valid).as("Demo qualification usable: "+grant.login()+"/"+grant.code()).isEqualTo(1);
        }
        System.out.println("DEMO_ROLE_QUALIFICATION_CHECK="+grants.size()+",CREATED="+created);
    }
}
