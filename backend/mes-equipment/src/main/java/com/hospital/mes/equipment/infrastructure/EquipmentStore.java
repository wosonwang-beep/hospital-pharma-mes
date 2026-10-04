package com.hospital.mes.equipment.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
@Repository @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EquipmentStore extends ScopedStore<EquipmentEntity> {
 public EquipmentStore(EquipmentMapper mapper){super(mapper,List.of("equipment_code","equipment_name","location"),Map.of("status","status","equipmentType","equipment_type","calibrationDueDate","calibration_due_date"));}
}
