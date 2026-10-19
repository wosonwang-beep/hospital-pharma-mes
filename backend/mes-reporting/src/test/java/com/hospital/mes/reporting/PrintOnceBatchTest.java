package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import com.hospital.mes.reporting.infrastructure.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrintOnceBatchTest {
 @Test void listPrintReturnsPdfWithoutArchiveAndRetainsSelectedOrder(){
  var templates=mock(PrintTemplateVersionMapper.class);var bindings=mock(PrintBindingMapper.class);var archives=mock(PrintBatchMapper.class);var printing=mock(PrintService.class);var provider=mock(PrintDataProvider.class);var renderer=mock(DocxRenderer.class);var converter=mock(PdfConverter.class);var mutations=mock(MasterMutation.class);
  when(mutations.context("print:document:generate")).thenReturn(new CurrentPlatformContext(1,2,Set.of(),Set.of("print:document:generate"),"session","request"));
  var template=new PrintTemplateVersion();template.setId(68L);template.setOrgId(1L);template.setBusinessType("INSPECTION_REPORT");template.setStatus("PUBLISHED");template.setPrintType("LIST");template.setDocx(new byte[]{1,2});template.setContentHash(PrintService.hash(template.getDocx()));
  when(templates.selectOne(any())).thenReturn(template);when(bindings.selectCount(any())).thenReturn(1L);when(printing.dataProvider("INSPECTION_REPORT",1,template)).thenReturn(provider);
  when(provider.fieldDefinitions()).thenReturn(InspectionSampleTemplate.dictionary());when(provider.fields()).thenReturn(InspectionSampleTemplate.fields());when(provider.itemFields()).thenReturn(InspectionSampleTemplate.itemFields());when(provider.example()).thenReturn(InspectionSampleTemplate.example(2));
  var first=new LinkedHashMap<>(InspectionSampleTemplate.example(2));first.put("reportNo","R-11");var second=new LinkedHashMap<>(first);second.put("reportNo","R-12");
  when(provider.load("11",false)).thenReturn(new PrintDataProvider.PrintSnapshot("1","R-11",first));when(provider.load("12",false)).thenReturn(new PrintDataProvider.PrintSnapshot("1","R-12",second));
  when(renderer.render(any(),anyMap(),anySet(),anyMap())).thenReturn(new byte[]{3});byte[] pdf="%PDF-current-list".getBytes();when(converter.convert(any())).thenReturn(pdf);
  var service=new PrintBatchService(printing,templates,bindings,archives,mutations,new ObjectMapper(),renderer,converter);
  assertArrayEquals(pdf,service.generate("INSPECTION_REPORT",List.of("12","11"),"68",false));verifyNoInteractions(archives);verify(mutations,never()).auditSnapshot(any(),any(),any(),any(),any(),any(),any(),any());
  var facts=org.mockito.ArgumentCaptor.forClass(Map.class);verify(renderer).render(any(),facts.capture(),anySet(),anyMap());assertTrue(facts.getValue().toString().indexOf("R-12")<facts.getValue().toString().indexOf("R-11"));
  assertThrows(IllegalArgumentException.class,()->service.generate("INSPECTION_REPORT",List.of("11"),"68",true));
 }
}
