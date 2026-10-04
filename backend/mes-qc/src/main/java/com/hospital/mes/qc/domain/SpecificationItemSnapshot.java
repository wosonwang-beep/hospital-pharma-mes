package com.hospital.mes.qc.domain;
import java.math.BigDecimal;
public record SpecificationItemSnapshot(long specificationItemId,String itemCode,String itemName,boolean required,String resultType,BigDecimal lowerLimit,BigDecimal upperLimit,Long unitId,String textAcceptanceCriteria,String methodCode,String methodVersion){}
