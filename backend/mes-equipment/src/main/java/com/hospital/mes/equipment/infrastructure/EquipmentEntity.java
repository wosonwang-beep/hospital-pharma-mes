package com.hospital.mes.equipment.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("md_equipment") public class EquipmentEntity extends ScopedEntity {
    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private String status;
    private java.time.LocalDate calibrationDueDate;
    private String location;
    public String getEquipmentCode(){return equipmentCode;} public void setEquipmentCode(String v){equipmentCode=v;}
    public String getEquipmentName(){return equipmentName;} public void setEquipmentName(String v){equipmentName=v;}
    public String getEquipmentType(){return equipmentType;} public void setEquipmentType(String v){equipmentType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public java.time.LocalDate getCalibrationDueDate(){return calibrationDueDate;} public void setCalibrationDueDate(java.time.LocalDate v){calibrationDueDate=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
 @Override public java.util.List<String> allowedActions(){return switch(status){case "ACTIVE" -> java.util.List.of("UPDATE","DISABLE","BEGIN_MAINTENANCE");case "MAINTENANCE" -> java.util.List.of("UPDATE","DISABLE","RETURN_TO_SERVICE");default -> java.util.List.of("UPDATE","ENABLE");};}
}
