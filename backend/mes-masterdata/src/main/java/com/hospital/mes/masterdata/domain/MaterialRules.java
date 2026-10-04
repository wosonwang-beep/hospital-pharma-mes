package com.hospital.mes.masterdata.domain;
import java.time.*;
import com.hospital.mes.common.exception.StateTransitionException;
public final class MaterialRules {
 private MaterialRules(){}
 public static boolean enabled(String state){return java.util.List.of("ACTIVE","DRAFT","APPROVED").contains(state);}
 public static String disable(String state){if(!enabled(state))throw new StateTransitionException("INVALID_STATE","Material is already inactive");return "INACTIVE";}
 public static void usable(String state,LocalDateTime from,LocalDateTime to,Instant at){var time=LocalDateTime.ofInstant(at,ZoneOffset.UTC);if(!enabled(state)||from!=null&&time.isBefore(from)||to!=null&&time.isAfter(to))throw new MasterGateException("MATERIAL_NOT_ACTIVE","SELECT_ACTIVE_EFFECTIVE_MATERIAL");}
 public static LocalDateTime time(String value){return value==null||value.isBlank()?null:LocalDateTime.ofInstant(Instant.parse(value),ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
 public static void period(LocalDateTime from,LocalDateTime to){if(from!=null&&to!=null&&from.isAfter(to))throw new IllegalArgumentException("Invalid effective period");}
 public static boolean required(Boolean value){if(value==null)throw new IllegalArgumentException("Boolean required");return value;}
}
