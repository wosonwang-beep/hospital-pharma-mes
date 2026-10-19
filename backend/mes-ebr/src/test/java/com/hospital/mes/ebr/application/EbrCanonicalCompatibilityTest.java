package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.process.application.ProcessQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Fixed Oct 7 published template10 shape and saved hash; no database or fixture rewrites. */
class EbrCanonicalCompatibilityTest {
    private static final String ORIGINAL_HASH="deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f";
    private final ObjectMapper json=new ObjectMapper();
    private EbrDefinitions definitions;
    private EbrService service;
    private MasterMutation mutations;
    private TemplateEntity template;
    private FormEntity form;
    private FieldEntity field;
    private ObjectNode fixture;
    private ObjectNode portable;

    @BeforeEach @SuppressWarnings("unchecked") void setup() throws Exception {
        try(var stream=getClass().getResourceAsStream("/legacy-template10-canonical.json")){
            assertNotNull(stream);fixture=(ObjectNode)json.readTree(stream);
        }
        template=new TemplateEntity();template.setId(10L);template.setOrgId(1L);
        template.setPackageVersionId(12L);template.setTemplateCode(fixture.path("templateCode").asText());
        template.setBusinessVersion(1);template.setStatus("EFFECTIVE");template.setContentHash(ORIGINAL_HASH);
        JsonNode storedForm=fixture.path("definition").path("forms").get(0);
        portable=json.createObjectNode();portable.put("schemaVersion","1.0");portable.set("form",storedForm.deepCopy());portable.putArray("rules");
        form=new FormEntity();form.setId(10L);form.setOrgId(1L);form.setTemplateVersionId(10L);
        form.setFormCode("MIX_RECORD");form.setFormName(storedForm.path("formName").asText());
        form.setOperationDefId(29L);form.setSchemaVersion("1.0");form.setSequenceNo(1);retainSchema();
        JsonNode storedField=storedForm.path("fields").get(0);
        field=new FieldEntity();field.setId(10L);field.setOrgId(1L);field.setFormDefId(10L);
        field.setFieldCode("OBS");field.setLabel(storedField.path("label").asText());field.setFieldType("TEXT");
        field.setSourceType("MANUAL");field.setDataType("STRING");field.setRequiredFlag(true);field.setReadonlyFlag(false);field.setSequenceNo(1);
        EbrStore db=mock(EbrStore.class);ScopedStore<TemplateEntity> templates=mock(ScopedStore.class);
        when(db.store(TemplateEntity.class)).thenReturn(templates);when(templates.get(1L,10L)).thenReturn(template);
        when(db.rows(FormEntity.class,1L,"template_version_id",10L)).thenReturn(List.of(form));
        when(db.rows(FieldEntity.class,1L,"form_def_id",10L)).thenReturn(List.of(field));
        ProcessQueryService processes=mock(ProcessQueryService.class);
        definitions=new EbrDefinitions(db,json,processes,null,null);
        mutations=new MasterMutation(null,null,null,json);
        service=new EbrService(db,definitions,null,mutations,processes,null,json,null);
    }
    private void retainSchema(){form.setSchemaJson(portable.toString());}
    private JsonNode canvas(String text){var n=json.createObjectNode();n.putObject("data").putArray("main").addObject().put("value",text);return n;}
    private void rejectPublished(){var ex=assertThrows(ComplianceException.class,()->service.published(1L,10L));assertEquals("Published definition hash mismatch",ex.getMessage());}

    @Test void historicalPublishedSampleReproducesOriginalHashAndPayload(){
        assertEquals(ORIGINAL_HASH,mutations.digest(fixture));
        assertEquals(fixture,definitions.canonical(template));
        assertEquals(ORIGINAL_HASH,service.published(1L,10L).path("definitionHash").asText());
        assertEquals(ORIGINAL_HASH,template.getContentHash());
    }
    @Test void explicitNullInHistoricalStoredSchemaIsKept(){
        ((ObjectNode)portable.path("form")).putNull("canvasDocument");retainSchema();
        assertTrue(definitions.canonical(template).path("definition").path("forms").get(0).has("canvasDocument"));
        assertEquals("155a47e5453d0e2eac9f622a7f4f3aa3899c9c74cca398043dfd5568564565c9",mutations.digest(definitions.canonical(template)));
        rejectPublished();
    }
    @Test void directCurrentDefinitionKeepsNullEvenWithOldLookingSchema(){
        template.setPackageVersionId(null);template.setProcessPackageId(12L);
        JsonNode canonical=definitions.canonical(template);
        assertTrue(canonical.path("definition").path("forms").get(0).has("canvasDocument"));
        template.setContentHash(mutations.digest(canonical));
        assertEquals(template.getContentHash(),service.published(1L,10L).path("definitionHash").asText());
    }
    @Test void realCurrentCanvasIsHashedAndTamperingIsRejected(){
        template.setPackageVersionId(null);template.setProcessPackageId(12L);
        ((ObjectNode)portable.path("form")).set("canvasDocument",canvas("published"));retainSchema();
        template.setContentHash(mutations.digest(definitions.canonical(template)));
        assertEquals(template.getContentHash(),service.published(1L,10L).path("definitionHash").asText());
        ((ObjectNode)portable.path("form")).set("canvasDocument",canvas("tampered"));retainSchema();rejectPublished();
    }
    @Test void addingCanvasToHistoricalFormCannotBeIgnored(){
        ((ObjectNode)portable.path("form")).set("canvasDocument",canvas("added"));retainSchema();rejectPublished();
    }
    @Test void changedNormalizedFieldCannotHideBehindPortableSchema(){field.setLabel("tampered");rejectPublished();}
    @Test void missingPortablePublicationEvidenceCannotActivateCompatibility(){
        form.setSchemaJson("{}");
        assertTrue(definitions.canonical(template).path("definition").path("forms").get(0).has("canvasDocument"));
        rejectPublished();
    }
    @Test void mismatchedSchemaOrFormCannotActivateCompatibility(){
        portable.put("schemaVersion","2.0");retainSchema();rejectPublished();
        portable.put("schemaVersion","1.0");((ObjectNode)portable.path("form")).put("formCode","OTHER");retainSchema();rejectPublished();
    }
}
