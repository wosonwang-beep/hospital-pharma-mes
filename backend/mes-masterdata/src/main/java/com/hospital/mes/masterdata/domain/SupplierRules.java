package com.hospital.mes.masterdata.domain;
import java.time.*;import com.hospital.mes.common.exception.StateTransitionException;
public final class SupplierRules {
 private SupplierRules(){}
 public static String qualify(String status,LocalDate to,Instant at){if(!"UNAPPROVED".equals(status))throw new StateTransitionException("INVALID_STATE","Only unapproved suppliers may be qualified");if(expired(to,at))throw new MasterGateException("SUPPLIER_NOT_APPROVED","RENEW_QUALIFICATION");return "APPROVED";}
 public static boolean expired(LocalDate to,Instant at){return to!=null&&at.atZone(ZoneOffset.UTC).toLocalDate().isAfter(to);}
 public static void usable(String status,LocalDate supplierTo,boolean approved,LocalDate relationTo,Instant at){if(!"APPROVED".equals(status)||expired(supplierTo,at)||!approved||expired(relationTo,at))throw new MasterGateException("SUPPLIER_NOT_APPROVED","SELECT_VALID_APPROVED_SUPPLIER");}
}
