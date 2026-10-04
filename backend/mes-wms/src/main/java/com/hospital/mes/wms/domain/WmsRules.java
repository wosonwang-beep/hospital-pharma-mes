package com.hospital.mes.wms.domain;

import com.hospital.mes.common.exception.*;
import java.math.*;
import java.time.*;

public final class WmsRules {
 private WmsRules() {}
 public static BigDecimal quantity(String value, boolean signed) {
  if(value==null || !value.matches(signed?"-?[0-9]+(\\.[0-9]{1,6})?":"[0-9]+(\\.[0-9]{1,6})?"))throw new IllegalArgumentException("Invalid quantity");
  BigDecimal n=new BigDecimal(value);
  if((signed?n.signum()==0:n.signum()<=0)||n.precision()-n.scale()>12)throw new IllegalArgumentException("Invalid quantity");
  return n;
 }
 public static BigDecimal exact(BigDecimal input,String factor,String rounded) {
  BigDecimal n=input.multiply(new BigDecimal(factor));
  if(n.compareTo(new BigDecimal(rounded))!=0)throw new ComplianceException("QUANTITY_PRECISION_LOSS","Unit conversion would lose quantity");
  try {n=n.setScale(6,RoundingMode.UNNECESSARY);}catch(ArithmeticException e){throw new ComplianceException("QUANTITY_PRECISION_LOSS","Ledger supports six decimal places");}
  if(n.precision()-n.scale()>12)throw new IllegalArgumentException("Quantity exceeds ledger capacity");return n;
 }
 public static void draft(String status){if(!"DRAFT".equals(status))throw new StateTransitionException("RECEIPT_IMMUTABLE","Only draft receipts can be changed");}
 public static void version(long actual,long expected){if(actual!=expected)throw new ResourceConflictException("VERSION_CONFLICT","Reload before retrying");}
 public static void dates(LocalDate manufacture,LocalDate expiry,LocalDate retest,LocalDate today){
  if(manufacture!=null&&manufacture.isAfter(today)||manufacture!=null&&expiry!=null&&expiry.isBefore(manufacture)||manufacture!=null&&retest!=null&&retest.isBefore(manufacture))throw new IllegalArgumentException("Invalid lot dates");
 }
 public static void movable(String status,LocalDate expiry,LocalDate retest,LocalDate today){
  if("FROZEN".equals(status))throw new ComplianceException("INVENTORY_FROZEN","Frozen inventory cannot be moved or adjusted");
  if(expiry!=null&&!expiry.isAfter(today)||retest!=null&&!retest.isAfter(today))throw new ComplianceException("MATERIAL_EXPIRED","Expiry or retest date reached");
 }
 public static void balance(BigDecimal current,BigDecimal delta){if(current.add(delta).signum()<0)throw new ComplianceException("INSUFFICIENT_INVENTORY","Movement would create negative inventory");}
 public static void check(Boolean value){if(value==null)throw new IllegalArgumentException("Receipt check is required");}
 public static void passed(Boolean value){if(!Boolean.TRUE.equals(value))throw new ComplianceException("RECEIPT_CHECK_FAILED","Every receipt check must pass before confirmation");}
}
