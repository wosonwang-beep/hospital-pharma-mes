package com.hospital.mes.equipment.application;
import com.hospital.mes.equipment.domain.EquipmentRules;
import com.hospital.mes.equipment.infrastructure.EquipmentStore;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EquipmentQueryService {
 private final EquipmentStore store;public EquipmentQueryService(EquipmentStore store){this.store=store;}
 public record EquipmentReference(String id,String code,String type,String status,long versionNo){}
 public EquipmentReference requireUsable(long org,long id,Instant at){var e=store.get(org,id);EquipmentRules.requireUsable(e.getStatus(),e.getCalibrationDueDate(),at);return new EquipmentReference(e.getId().toString(),e.getEquipmentCode(),e.getEquipmentType(),e.getStatus(),e.getVersionNo());}
 public record EquipmentEvidence(String equipmentId,long versionNo,String equipmentType,String status,java.time.LocalDate calibrationDueDate,Instant checkedAt){}
 public EquipmentEvidence equipmentEvidence(long org,long id,Instant at){var e=store.get(org,id);EquipmentRules.requireUsable(e.getStatus(),e.getCalibrationDueDate(),at);return new EquipmentEvidence(e.getId().toString(),e.getVersionNo(),e.getEquipmentType(),e.getStatus(),e.getCalibrationDueDate(),at);}
}
