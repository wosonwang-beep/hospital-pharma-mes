package com.hospital.mes.ebr;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.MesApplication;
import com.hospital.mes.ebr.application.EbrQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Native retained-definition read only: no web port, schema change, or regulated write. */
@SpringBootTest(classes=MesApplication.class,webEnvironment=SpringBootTest.WebEnvironment.MOCK,
    properties="spring.flyway.enabled=false")
@ActiveProfiles("local")
@EnabledIfSystemProperty(named="mes.local.ebr.canonical",matches="true")
class EbrPublishedCanonicalLocalIT {
    @Autowired EbrQueryService ebr;
    @Autowired JdbcTemplate jdbc;
    @Test @Transactional(readOnly=true)
    void retainedPublishedDefinitionIsConsumableWithoutChangingItsHashOrRows(){
        assertThat(jdbc.queryForObject("SELECT DATABASE()",String.class)).isEqualTo("hospital_pharma_mes_dev");
        assertThat(jdbc.queryForObject("SELECT @@foreign_key_checks",Integer.class)).isEqualTo(1);
        String hash="deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f";
        var before=jdbc.queryForList("SELECT id,package_version_id,process_package_id,status,content_hash,version_no FROM ebr_template_version WHERE id=10");
        String schemaBefore=jdbc.queryForObject("SELECT SHA2(CAST(schema_json AS BINARY),256) FROM ebr_form_def WHERE template_version_id=10",String.class);
        var published=ebr.requirePublished(1,10);
        assertThat(published.path("definitionHash").asText()).isEqualTo(hash);
        assertThat(published.path("packageVersionId").asText()).isEqualTo("12");
        assertThat(published.has("processPackageId")).isFalse();
        assertThat(published.path("definition").path("forms").get(0).has("canvasDocument")).isFalse();
        assertThat(jdbc.queryForList("SELECT id,package_version_id,process_package_id,status,content_hash,version_no FROM ebr_template_version WHERE id=10")).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT SHA2(CAST(schema_json AS BINARY),256) FROM ebr_form_def WHERE template_version_id=10",String.class)).isEqualTo(schemaBefore);
    }
}
