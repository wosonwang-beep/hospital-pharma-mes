package com.hospital.mes.qc.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Product owner supplies a same-org usable product; QC does not query another module's tables. */
public interface QcProductReferencePort { JsonNode product(long organizationId,long productId,boolean lock); }
