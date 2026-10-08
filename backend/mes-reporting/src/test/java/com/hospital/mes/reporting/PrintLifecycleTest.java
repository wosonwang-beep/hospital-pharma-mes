package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.reporting.infrastructure.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.common.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
class PrintLifecycleTest {
 PrintTemplateVersionMapper templates;PrintBindingMapper bindings;PrintArtifactMapper artifacts;PdfConverter converter;PrintDataProvider provider;PrintService service;Set<String> rights;
 @BeforeEach void init(){rights=Set.of("print:template:manage","print:template:publish","print:template:view","print:document:generate");templates=mock(PrintTemplateVersionMapper.class);bindings=mock(PrintBindingMapper.class);artifacts=mock(PrintArtifactMapper.class);converter=mock(PdfConverter.class);provider=mock(PrintDataProvider.class);when(provider.businessType()).thenReturn("INSPECTION_REPORT");when(provider.fields()).thenReturn(InspectionSampleTemplate.fields());when(provider.itemFields()).thenReturn(InspectionSampleTemplate.itemFields());when(provider.example()).thenReturn(InspectionSampleTemplate.example(2));var json=new ObjectMapper();var mutation=new MasterMutation(()->new CurrentPlatformContext(1,2,Set.of("QA"),rights,"session","request"),mock(PlatformIdempotencyService.class),mock(AuditApplicationService.class),json);service=new PrintService(templates,bindings,artifacts,mutation,json,new DocxGuard(),new DocxRenderer(new DocxGuard()),converter,List.of(provider));}
 PrintTemplateVersion template(String status){var row=new PrintTemplateVersion();row.setId(10L);row.setOrgId(1L);row.setCreatedBy(2L);row.setVersionNo(0L);row.setTemplateRevision(1L);row.setTemplateCode("REPORT");row.setTemplateName("报告");row.setBusinessType("INSPECTION_REPORT");row.setStatus(status);row.setDocx(InspectionSampleTemplate.create());when(templates.selectOne(any())).thenReturn(row);return row;}
 @Test void importedOriginalDocxRetainsExactOoxmlBytesWithoutNativeConversion(){
  var previous=template("PUBLISHED");
  when(templates.selectList(any())).thenReturn(List.of(previous));
  when(templates.insert(any(PrintTemplateVersion.class))).thenAnswer(call->{var row=(PrintTemplateVersion)call.getArgument(0);row.setId(125L);return 1;});
  byte[] original=InspectionSampleTemplate.create();
  var saved=service.upload("REPORT","原版 Word 表格","INSPECTION_REPORT",original,"导入保留原表格");
  assertEquals("125",saved.path("id").asText());
  var inserted=org.mockito.ArgumentCaptor.forClass(PrintTemplateVersion.class);
  verify(templates).insert(inserted.capture());
  assertArrayEquals(original,inserted.getValue().getDocx(),"Original Word OOXML must be byte-for-byte retained");
  assertEquals("DRAFT",inserted.getValue().getStatus());
  assertEquals(previous.getTemplateRevision()+1,inserted.getValue().getTemplateRevision());
  assertEquals("PUBLISHED",previous.getStatus());
  assertThrows(IllegalArgumentException.class,()->new NativePrintDesigner(new ObjectMapper()).extract(original));
  verifyNoInteractions(converter);
 }
 @Test void nativeVisualDesignerAppendsNewDraftWithoutOverwritingPublishedTemplate(){
  when(provider.fieldDefinitions()).thenReturn(InspectionSampleTemplate.dictionary());
  var previous=template("PUBLISHED");
  when(templates.selectList(any())).thenReturn(List.of(previous));
  when(templates.insert(any(PrintTemplateVersion.class))).thenAnswer(call->{var row=(PrintTemplateVersion)call.getArgument(0);row.setId(124L);return 1;});
  var design=new NativePrintDesigner(new ObjectMapper()).defaults();
  var saved=service.createNativeDesign("REPORT","报告新版","INSPECTION_REPORT",design,"可视化页面设计");
  assertEquals("124",saved.path("id").asText());
  var captured=org.mockito.ArgumentCaptor.forClass(PrintTemplateVersion.class);
  verify(templates).insert(captured.capture());
  var newVersion=captured.getValue();
  assertEquals("DRAFT",newVersion.getStatus());
  assertEquals(previous.getTemplateRevision()+1,newVersion.getTemplateRevision());
  assertEquals("PUBLISHED",previous.getStatus());
  assertNotEquals(PrintService.hash(previous.getDocx()),PrintService.hash(newVersion.getDocx()));
  assertEquals(design,new NativePrintDesigner(new ObjectMapper()).extract(newVersion.getDocx()));
 }
 @Test void permissionDenialPreventsAnyWrite(){rights=Set.of();assertThrows(PermissionException.class,()->service.upload("A","报告","INSPECTION_REPORT",InspectionSampleTemplate.create(),"创建"));verifyNoInteractions(templates,bindings,artifacts,converter);}
 @Test void publishedVersionCannotBeValidatedOrOverwritten(){template("PUBLISHED");assertThrows(ComplianceException.class,()->service.validate("10",0,"验证"));verifyNoInteractions(converter);verify(templates,never()).update(any(),any());}
 @Test void publishRequiresRealPreviewState(){template("DRAFT");assertThrows(ComplianceException.class,()->service.publish("10",0,"发布"));verify(templates,never()).update(any(),any());}
 @Test void optimisticVersionConflictStopsPublish(){template("VALIDATED");assertThrows(ResourceConflictException.class,()->service.publish("10",3,"发布"));}
 @Test void incompatibleBusinessBindingDenied(){template("PUBLISHED");assertThrows(ComplianceException.class,()->service.bind("10","OTHER",true,"绑定"));verifyNoInteractions(bindings);}
 @Test void conversionFailureLeavesNoArtifact(){template("PUBLISHED");when(bindings.selectCount(any())).thenReturn(1L);when(provider.load("22",false)).thenReturn(new PrintDataProvider.PrintSnapshot("4","R",InspectionSampleTemplate.example(2)));when(converter.convert(any())).thenThrow(new ComplianceException("PRINT_CONVERSION_FAILED","timeout"));assertThrows(ComplianceException.class,()->service.generate("INSPECTION_REPORT","22","10",false));verify(artifacts,never()).insert(any(PrintArtifact.class));}
 @Test void formalReprintReusesPersistedArtifactBeforeInactiveTemplateCheck(){when(provider.load("22",true)).thenReturn(new PrintDataProvider.PrintSnapshot("4","R",InspectionSampleTemplate.example(2)));var a=artifact();when(artifacts.selectOne(any())).thenReturn(a);assertEquals("50",service.generate("INSPECTION_REPORT","22","10",true).path("id").asText());verifyNoInteractions(templates,converter,bindings);}
 PrintArtifact artifact(){var a=new PrintArtifact();a.setId(50L);a.setOrgId(1L);a.setCreatedBy(2L);a.setVersionNo(0L);a.setBusinessType("INSPECTION_REPORT");a.setBusinessId("22");a.setBusinessVersion("4");a.setReportNo("R");a.setTemplateVersionId(10L);a.setTemplateRevision(1L);a.setFormal(true);a.setPdf("persisted-archive-bytes".getBytes());a.setPdfHash(PrintService.hash(a.getPdf()));return a;}
 @Test void archiveReadChecksBusinessPermissionAndReturnsExactStoredBytes(){var a=artifact();when(artifacts.selectOne(any())).thenReturn(a);assertArrayEquals(a.getPdf(),service.pdf("50"));verify(provider).authorizeRead("22");verifyNoInteractions(converter);}
 @Test void revokedBusinessPermissionDeniesArchive(){when(artifacts.selectOne(any())).thenReturn(artifact());doThrow(new PermissionException("PERMISSION_DENIED","revoked")).when(provider).authorizeRead("22");assertThrows(PermissionException.class,()->service.pdf("50"));}
 @Test void corruptArchiveRejected(){var a=artifact();a.setPdf("tampered".getBytes());when(artifacts.selectOne(any())).thenReturn(a);assertThrows(ComplianceException.class,()->service.pdf("50"));}
}
