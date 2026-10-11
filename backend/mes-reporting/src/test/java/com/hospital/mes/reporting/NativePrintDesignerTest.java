package com.hospital.mes.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.reporting.application.*;
import org.junit.jupiter.api.Test;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.ByteArrayInputStream;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NativePrintDesignerTest {
 final ObjectMapper json=new ObjectMapper();
 final NativePrintDesigner designer=new NativePrintDesigner(json);
 @Test void defaultChineseDesignRoundtripsToRealDocxAndAllDynamicFieldsRender() throws Exception {
  var defaultDesign=designer.defaults();
  byte[] bytes=designer.generate(defaultDesign,InspectionSampleTemplate.dictionary());
  assertArrayEquals(bytes,bytes.clone());
  assertEquals(defaultDesign,designer.extract(bytes));
  Set<String> tags=new DocxGuard().validate(bytes,InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  assertTrue(tags.containsAll(Set.of("modeLabel","reportNo","items")));
  byte[] result=new DocxRenderer(new DocxGuard()).render(bytes,InspectionSampleTemplate.example(3),
   InspectionSampleTemplate.fields(),InspectionSampleTemplate.itemFields());
  try(var doc=new XWPFDocument(new ByteArrayInputStream(result))){
   assertEquals(4,doc.getTables().getFirst().getNumberOfRows());
   assertTrue(doc.getParagraphs().stream().anyMatch(p->p.getText().contains("药品检验报告")));
   assertFalse(doc.getParagraphs().stream().anyMatch(p->p.getText().contains("{{")));
  }
 }
 @Test void rejectsUnapprovedFieldsAndExpressionsAndBrokenLayouts() {
  var root=designer.defaults().deepCopy();
  ((ObjectNode)root.path("blocks").get(2)).put("fieldKey","java.lang.Runtime");
  assertThrows(IllegalArgumentException.class,()->designer.generate(root,InspectionSampleTemplate.dictionary()));
  var root2=designer.defaults().deepCopy();
  ((ObjectNode)root2.path("blocks").get(0)).put("text","{{T(Runtime)}}");
  assertThrows(IllegalArgumentException.class,()->designer.generate(root2,InspectionSampleTemplate.dictionary()));
  var root3=designer.defaults().deepCopy();
  ((ObjectNode)root3.path("blocks").get(2)).put("id","heading");
  assertThrows(IllegalArgumentException.class,()->designer.generate(root3,InspectionSampleTemplate.dictionary()));
  var root4=designer.defaults().deepCopy();
  ((ObjectNode)root4.path("blocks").get(1)).put("fieldKey","materialName");
  assertThrows(IllegalArgumentException.class,()->designer.generate(root4,InspectionSampleTemplate.dictionary()));
 }
 @Test void rejectsOpeningOrdinaryUploadedDocxWithoutNativeDesignerMetadata() {
  byte[] ordinary=InspectionSampleTemplate.create();
  assertThrows(IllegalArgumentException.class,()->designer.extract(ordinary));
 }

 @org.junit.jupiter.api.Test void defaultsOnlyUseAuthorizedMetadataAndIndependentDataset(){
  var designer=new com.hospital.mes.reporting.application.NativePrintDesigner(new com.fasterxml.jackson.databind.ObjectMapper());
  var fields=java.util.List.of(new com.hospital.mes.reporting.application.PrintField("modeLabel","打印状态","打印信息","","草稿",false),new com.hospital.mes.reporting.application.PrintField("reportNo","单据编号","打印信息","","R",false),new com.hospital.mes.reporting.application.PrintField("receiptNo","收货单号","主信息","","R",false),new com.hospital.mes.reporting.application.PrintField("qty","数量","明细","","1",true,"NUMBER",null,"lines"));
  var spec=designer.defaults("收货单",fields);
  org.assertj.core.api.Assertions.assertThat(spec.toString()).contains("receiptNo","lines").doesNotContain("requestNo","overallResult");
  org.assertj.core.api.Assertions.assertThat(designer.generate(spec,fields).length).isGreaterThan(1000);
 }
}
