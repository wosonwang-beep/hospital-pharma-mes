package com.hospital.mes.reporting.application;
import java.util.*;
/** Business adapters own authorization, state, signature verification and complete server-side reads. */
public interface PrintDataProvider {
 String businessType();
 default String moduleLabel(){return "其他业务";}
 default String documentLabel(){return businessType();}
 java.util.List<PrintField> fieldDefinitions();
 Set<String> fields();
 Set<String> itemFields();
 Map<String,Object> example();
 void authorizeRead(String businessId);
 PrintSnapshot load(String businessId, boolean formal);
 record PrintSnapshot(String businessVersion, String reportNo, Map<String,Object> data) {
  public PrintSnapshot { data=Map.copyOf(data); }
 }
}
