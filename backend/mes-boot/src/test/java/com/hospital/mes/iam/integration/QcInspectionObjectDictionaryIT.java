package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class QcInspectionObjectDictionaryIT {
 @Autowired JdbcTemplate jdbc;

 @Test void fixedQualityObjectDictionaryIsSeededExactlyOnce() {
  var type=jdbc.queryForMap("SELECT id,dict_kind,structure_type,status FROM sys_dict_type WHERE org_id=1 AND dict_code='QC_OBJECT_TYPE'");
  assertThat(type.get("dict_kind")).isEqualTo("SYSTEM");
  assertThat(type.get("structure_type")).isEqualTo("FLAT");
  assertThat(type.get("status")).isEqualTo("ACTIVE");
  var codes=jdbc.queryForList("SELECT item_code FROM sys_dict_item WHERE org_id=1 AND dict_type_id=? AND status='ACTIVE'",
    String.class,type.get("id"));
  assertThat(codes).containsExactlyInAnyOrderElementsOf(
    Set.of("MATERIAL","FINISHED_PRODUCT","INTERMEDIATE","ENVIRONMENT","PROCESS"));
  assertThat(jdbc.queryForObject(
    "SELECT COUNT(*) FROM sys_dict_item WHERE org_id=1 AND dict_type_id=?",Long.class,type.get("id"))).isEqualTo(5);
 }

 @Test void historicalQualityDataIsNotRewrittenByDictionarySeed() {
  assertThat(jdbc.queryForObject("SELECT success FROM flyway_schema_history WHERE version='053'",Integer.class)).isEqualTo(1);

 }
}
