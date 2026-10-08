package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BookDefinitionTest {
 private final ObjectMapper json=new ObjectMapper();
 private String valid(){return """
 {"varietyCode":"SYNTHETIC","varietyName":"合成制剂品种","mappings":[{"productId":"1","packageVersionId":"2","ebrTemplateVersionId":"3"},{"productId":"4","packageVersionId":"5","ebrTemplateVersionId":"6"}],"entries":[{"code":"PREPARE","chapter":"过程记录","title":"配制记录","kind":"FORM","order":1,"required":true,"minCount":1,"scope":"OPERATION","operationCode":"PREPARE","formCode":"F_PREPARE","fields":["ACTUAL"],"attachmentIds":[]},{"code":"FILL","chapter":"过程记录","title":"分装记录","kind":"FORM","order":2,"required":true,"minCount":1,"scope":"BATCH","operationCode":"FILL","formCode":"F_FILL","fields":["ACTUAL"],"attachmentIds":[]}]}
 """;}
 @Test void oneVarietyAcceptsTwoExplicitSpecificationMappings()throws Exception{var d=BookDefinition.parse(json.readTree(valid()),json);assertEquals(2,d.mappings().size());assertEquals(java.util.Set.of("F_FILL"),d.batchFormCodes());}
 @Test void unknownTopLevelCannotBecomeExecutableConfiguration()throws Exception{assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(((com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(valid()).deepCopy()).put("sql","SELECT *"),json));}
 @Test void duplicateProductsAndFormCodesRejected()throws Exception{var n=json.readTree(valid());((com.fasterxml.jackson.databind.node.ObjectNode)n.path("mappings").get(1)).put("productId","1");assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));}
 @Test void templateRequiresExplicitStableFormFields()throws Exception{var n=json.readTree(valid());((com.fasterxml.jackson.databind.node.ObjectNode)n.path("entries").get(0)).putArray("fields");assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));}
 @Test void arbitrarySourceAndMalformedAttachmentIdsReject()throws Exception{var n=json.readTree(valid());((com.fasterxml.jackson.databind.node.ObjectNode)n.path("entries").get(0)).put("kind","SQL");assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));}
}
