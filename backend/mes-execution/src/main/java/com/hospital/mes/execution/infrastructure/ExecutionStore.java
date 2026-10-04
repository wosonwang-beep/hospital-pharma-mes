package com.hospital.mes.execution.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import java.util.*;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ExecutionStore {
 public final OperationMapper operationMapper; public final ScopedStore<OperationEntity> operations;
 public final EquipmentUsageMapper equipmentUsageMapper; public final ScopedStore<EquipmentUsageEntity> equipmentUsages;
 public final EquipmentRunMapper equipmentRunMapper; public final ScopedStore<EquipmentRunEntity> equipmentRuns;
 public final ParameterValueMapper parameterValueMapper; public final ScopedStore<ParameterValueEntity> parameterValues;
 public final WeighingMapper weighingMapper; public final ScopedStore<WeighingEntity> weighings;
 public final ChargeMapper chargeMapper; public final ScopedStore<ChargeEntity> charges;
 public final QuantityEventMapper quantityEventMapper; public final ScopedStore<QuantityEventEntity> quantityEvents;
 public final GenealogyMapper genealogyMapper; public final ScopedStore<GenealogyEntity> genealogys;
 public ExecutionStore(OperationMapper operation,EquipmentUsageMapper equipmentUsage,EquipmentRunMapper equipmentRun,ParameterValueMapper parameterValue,WeighingMapper weighing,ChargeMapper charge,QuantityEventMapper quantityEvent,GenealogyMapper genealogy){
 this.operationMapper=operation;this.operations=new ScopedStore<>(operation,List.of(),Map.of());
 this.equipmentUsageMapper=equipmentUsage;this.equipmentUsages=new ScopedStore<>(equipmentUsage,List.of(),Map.of());
 this.equipmentRunMapper=equipmentRun;this.equipmentRuns=new ScopedStore<>(equipmentRun,List.of(),Map.of());
 this.parameterValueMapper=parameterValue;this.parameterValues=new ScopedStore<>(parameterValue,List.of(),Map.of());
 this.weighingMapper=weighing;this.weighings=new ScopedStore<>(weighing,List.of(),Map.of());
 this.chargeMapper=charge;this.charges=new ScopedStore<>(charge,List.of(),Map.of());
 this.quantityEventMapper=quantityEvent;this.quantityEvents=new ScopedStore<>(quantityEvent,List.of(),Map.of());
 this.genealogyMapper=genealogy;this.genealogys=new ScopedStore<>(genealogy,List.of(),Map.of());
 }
}
