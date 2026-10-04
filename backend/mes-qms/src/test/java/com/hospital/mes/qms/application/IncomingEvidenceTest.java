package com.hospital.mes.qms.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.signature.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class IncomingEvidenceTest {
 @Test void lotQuantityRetainsExactDecimalStringAcrossSignaturePersistence() throws Exception {
  var json=new ObjectMapper();
  var lot=new IncomingMaterialPort.LotFacts(1,2,3,4,5,6,new BigDecimal("1234567890123456.12345678"),7,2,true,true,true,true,"PENDING_QA_RELEASE","BLOCKED",null,null,8);
  var record=IncomingQualityService.lotEvidence(json,lot);
  assertTrue(record.path("quantity").isTextual());
  assertEquals("1234567890123456.12345678",record.path("quantity").asText());
  var object=new SignableObject("MATERIAL_RELEASE_DECISION","1:8",8,record,List.of("MaterialLot:1"));
  var canonicalizer=new Rfc8785SignatureCanonicalizer(json);
  var saved=json.readValue(json.writeValueAsString(object),SignableObject.class);
  assertEquals(canonicalizer.digest(object),canonicalizer.digest(saved));
 }
}