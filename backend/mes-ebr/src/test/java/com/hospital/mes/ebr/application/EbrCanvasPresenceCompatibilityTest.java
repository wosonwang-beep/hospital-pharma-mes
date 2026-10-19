package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.ebr.domain.EbrCommands.Form;
import com.hospital.mes.ebr.infrastructure.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real JSON input semantics and retained-row reading, without rewriting golden evidence. */
class EbrCanvasPresenceCompatibilityTest {
 private final ObjectMapper json=new ObjectMapper();
 private final EbrStore store=mock(EbrStore.class);
 private final EbrDefinitions definitions=new EbrDefinitions(store,json,null,null,null);
 private ObjectNode form() throws Exception {return (ObjectNode)json.readTree("{\"formCode\":\"F\",\"formName\":\"Form\",\"operationDefId\":null,\"schemaVersion\":\"1.0\",\"sequenceNo\":1,\"fields\":[]}");}
 private FormEntity retained(ObjectNode schema){var row=new FormEntity();row.setId(1L);row.setOrgId(1L);row.setTemplateVersionId(1L);row.setFormCode("F");row.setFormName("Form");row.setSchemaVersion("1.0");row.setSequenceNo(1);row.setSchemaJson(schema.toString());return row;}
 private Form load(FormEntity row){var template=new TemplateEntity();template.setId(1L);template.setOrgId(1L);template.setDefinitionSchemaVersion(1);when(store.rows(FormEntity.class,1L,"template_version_id",1L)).thenReturn(List.of(row));return definitions.load(template).forms().getFirst();}
 @Test void absentCanvasSurvivesReadPayloadAndSaveWithoutFilteringOtherNulls() throws Exception {
  var original=form();var row=retained(original);Form loaded=load(row);assertNull(loaded.canvasDocument());JsonNode payload=json.valueToTree(loaded);assertFalse(payload.has("canvasDocument"));assertTrue(payload.has("operationDefId"));assertTrue(payload.path("operationDefId").isNull());
  Form input=json.treeToValue(payload,Form.class);assertNull(input.canvasDocument());assertEquals(original,json.readTree(definitions.encodeFormForSave(input,row)));
 }
 @Test void originalExplicitNullRemainsPresentEvenWhenInputOmitsCanvas() throws Exception {
  var original=form();original.putNull("canvasDocument");var row=retained(original);Form loaded=load(row);assertNotNull(loaded.canvasDocument());assertTrue(loaded.canvasDocument().isNull());assertTrue(json.valueToTree(loaded).has("canvasDocument"));assertEquals(original,json.readTree(definitions.encodeFormForSave(loaded,row)));
  var omitted=original.deepCopy();omitted.remove("canvasDocument");assertEquals(original,json.readTree(definitions.encodeFormForSave(json.treeToValue(omitted,Form.class),row)));
 }
 @Test void realCanvasIsPreservedEditableAndExplicitlyClearable() throws Exception {
  var original=form();var canvas=json.readTree("{\"data\":{\"main\":[{\"value\":\"Original\",\"control\":{\"id\":\"C\",\"required\":false}}]},\"options\":{\"margin\":12.5,\"optional\":null},\"empty\":[]}");original.set("canvasDocument",canvas);var row=retained(original);assertEquals(canvas,load(row).canvasDocument());
  var omitted=original.deepCopy();omitted.remove("canvasDocument");assertEquals(original,json.readTree(definitions.encodeFormForSave(json.treeToValue(omitted,Form.class),row)));
  var edited=original.deepCopy();((ObjectNode)edited.path("canvasDocument").path("data").path("main").get(0)).put("value","Edited");assertEquals(edited,json.readTree(definitions.encodeFormForSave(json.treeToValue(edited,Form.class),row)));
  var cleared=original.deepCopy();cleared.putNull("canvasDocument");Form clearInput=json.treeToValue(cleared,Form.class);assertNotNull(clearInput.canvasDocument());assertTrue(clearInput.canvasDocument().isNull());assertEquals(cleared,json.readTree(definitions.encodeFormForSave(clearInput,row)));
 }
}
