package com.hospital.mes.reporting;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.common.exception.PermissionException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.apache.pdfbox.Loader;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Actual existing DEV records, no seed/import/business writes. Transaction ends with rollback. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class PrintOnceReadOnlyIT {
 @Autowired PrintService printing; @Autowired PrintBatchService batches; @Autowired JdbcTemplate jdbc;
 @MockitoBean CurrentPlatformContextResolver contexts;
 Set<String> rights;
 @BeforeEach void context(){rights=new HashSet<>(Set.of("print:document:generate","print:template:view","wms:receipt:view","qms:report:view","qms:inspection:view","qms:incoming:view","qms:quality:view","qms:production:view","qms:finished:view","print:source:view"));as(1);}
 void as(long org){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,1,Set.of(),rights,"print-once-read","print-once-read"));}
 Map<String,Long> counts(){var out=new LinkedHashMap<String,Long>();for(var table:List.of("mes_print_artifact","mes_print_batch","mes_print_artifact_source_evidence","mes_print_batch_source_evidence","gxp_audit_event","gxp_signature"))out.put(table,jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class));return out;}
 @Test void singleAndBatchReturnRealCurrentPdfWithoutPersistence()throws Exception{
  var before=counts();byte[] single=printing.generate("WMS_RECEIPT","22","68",false);byte[] batch=batches.generate("WMS_RECEIPT",List.of("25","22"),"68",false);
  try(var pdf=Loader.loadPDF(single)){assertThat(pdf.getNumberOfPages()).isPositive();}
  try(var pdf=Loader.loadPDF(batch)){assertThat(pdf.getNumberOfPages()).isEqualTo(2);}
  assertThat(counts()).isEqualTo(before);
 }
 @Test void preservedHistoricalFilesAuthorizeAndReturnExactBytes(){
  var before=counts();int read=0;long bytes=0;
  for(var table:List.of("mes_print_artifact","mes_print_batch"))for(var row:jdbc.queryForList("SELECT id,pdf_hash,OCTET_LENGTH(pdf) bytes FROM "+table+" WHERE template_version_id IN (10,30,31,32,33,40,41,42,43,44) ORDER BY id")){
   byte[] pdf=table.equals("mes_print_artifact")?printing.pdf(row.get("id").toString()):batches.pdf(row.get("id").toString());
   assertThat(PrintService.hash(pdf)).isEqualTo(row.get("pdf_hash"));assertThat(pdf.length).isEqualTo(((Number)row.get("bytes")).intValue());read++;bytes+=pdf.length;
  }
  assertThat(read).isEqualTo(18);assertThat(bytes).isEqualTo(549200);assertThat(counts()).isEqualTo(before);
 }
 @Test void originalBusinessPermissionsAndOrganizationStillRestrictAccess(){
  rights.remove("wms:receipt:view");as(1);assertThatThrownBy(()->printing.generate("WMS_RECEIPT","22","68",false)).isInstanceOf(PermissionException.class);
  assertThatThrownBy(()->printing.pdf("7")).isInstanceOf(PermissionException.class);
  rights.add("wms:receipt:view");as(2);assertThatThrownBy(()->printing.pdf("7")).isInstanceOf(NoSuchElementException.class);
 }
}
