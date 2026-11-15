package com.hospital.mes.qc.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Immutable copy of a QC quality standard captured the moment a downstream document freezes it
 * (DCP-QC-SPEC-BASIC-LIBRARY-001). A frozen document keeps its own snapshot and never re-reads a live
 * standard to prove its content, so later maintenance of the basic library cannot rewrite history.
 */
public record QcStandardSnapshot(int schemaVersion,long specificationId,String specificationCode,String specificationName,
 String basisType,String basisReference,LocalDate effectiveDate,List<String> materials,List<String> products,
 List<SpecificationItemSnapshot> items){
 public QcStandardSnapshot{schemaVersion=schemaVersion>0?schemaVersion:1;materials=List.copyOf(materials);products=List.copyOf(products);items=List.copyOf(items);}
}
