package com.hospital.mes.reporting;
import com.hospital.mes.reporting.application.*;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class IndependentDatasetDocxTest {
 private final DocxGuard guard=new DocxGuard();
 private byte[] document(boolean mix)throws Exception{try(var d=new XWPFDocument();var out=new ByteArrayOutputStream()){for(var dataset:List.of("items","checks")){var table=d.createTable(2,2);table.getRow(0).getCell(0).setText(dataset.equals("items")?"收货明细":"检验记录");table.getRow(1).getCell(0).setText("{{"+dataset+"}}");table.getRow(1).getCell(1).setText("["+(dataset.equals("items")?"materialName":mix?"materialName":"itemName")+"]");}d.write(out);return out.toByteArray();}}
 @Test void twoCollectionsRenderIndependentlyWithoutMultiplyingRows()throws Exception{var docx=document(false);var rendered=new DocxRenderer(guard).render(docx,Map.of("items",List.of(Map.of("materialName","原料甲"),Map.of("materialName","原料乙")),"checks",List.of(Map.of("itemName","检查一"),Map.of("itemName","检查二"),Map.of("itemName","检查三"))),Set.of("items","checks"),Map.of("items",Set.of("materialName"),"checks",Set.of("itemName")));try(var d=new XWPFDocument(new ByteArrayInputStream(rendered))){assertThat(d.getTables()).hasSize(2);assertThat(d.getTables().get(0).getRows()).hasSize(3);assertThat(d.getTables().get(1).getRows()).hasSize(4);assertThat(d.getTables().get(0).getText()).contains("原料甲","原料乙").doesNotContain("检查");assertThat(d.getTables().get(1).getText()).contains("检查一","检查二","检查三").doesNotContain("原料");}}
 @Test void crossCollectionFieldBindingFailsBeforeRendering()throws Exception{byte[] docx=document(true);assertThatThrownBy(()->guard.validate(docx,Set.of("items","checks"),Map.of("items",Set.of("materialName"),"checks",Set.of("itemName")))).hasMessageContaining("其他集合");}
}
