package com.hospital.mes.equipment.domain;
import com.hospital.mes.masterdata.domain.MasterGateException;
import com.hospital.mes.common.exception.StateTransitionException;
import java.time.*;
public final class EquipmentRules {
 private EquipmentRules(){}
 public static void requireUsable(String status,LocalDate due,Instant at){if(!"ACTIVE".equals(status))throw new MasterGateException("EQUIPMENT_UNAVAILABLE","RESTORE_EQUIPMENT");if(due!=null&&due.isBefore(at.atZone(ZoneOffset.UTC).toLocalDate()))throw new MasterGateException("EQUIPMENT_CALIBRATION_EXPIRED","CALIBRATE_EQUIPMENT");}
 public static String beginMaintenance(String state){if(!"ACTIVE".equals(state))throw new StateTransitionException("INVALID_STATE","Active equipment required");return "MAINTENANCE";}
 public static String returnToService(String state){if(!"MAINTENANCE".equals(state))throw new StateTransitionException("INVALID_STATE","Maintenance equipment required");return "ACTIVE";}
}
