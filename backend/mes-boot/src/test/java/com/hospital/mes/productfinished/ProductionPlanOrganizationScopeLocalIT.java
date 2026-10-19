package com.hospital.mes.productfinished;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.MesApplication;
import com.hospital.mes.production.application.ProductionQueryService;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Read-only native DB check of the exact scoped lock consumed by plan commands. */
@SpringBootTest(classes=MesApplication.class,webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("local")
@EnabledIfSystemProperty(named="mes.local.plan.scope",matches="true")
class ProductionPlanOrganizationScopeLocalIT {
    @Autowired ProductionQueryService production;
    @Autowired JdbcTemplate jdbc;
    @Test @Transactional
    void foreignOrganizationCannotResolveOrLockTheExistingInstruction(){
        assertThat(jdbc.queryForObject("SELECT DATABASE()",String.class)).isEqualTo("hospital_pharma_mes_dev");
        assertThat(jdbc.queryForObject("SELECT @@foreign_key_checks",Integer.class)).isEqualTo(1);
        long org=jdbc.queryForObject("SELECT org_id FROM prd_main_batch WHERE id=230",Long.class);
        assertThat(production.lockBatch(org,230).mainBatchId()).isEqualTo(230);
        assertThatThrownBy(()->production.lockBatch(org+1,230)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(()->production.batch(org+1,230)).isInstanceOf(NoSuchElementException.class);
    }
}
