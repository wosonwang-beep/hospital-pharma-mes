package com.hospital.mes.equipment.domain;
import static org.assertj.core.api.Assertions.*;
import java.time.*;
import org.junit.jupiter.api.Test;
class EquipmentRulesTest {
 @Test void tcEqp001RejectsExpiredCalibrationAndProvidesRequiredAction(){var at=Instant.parse("2026-10-03T00:00:00Z");assertThatThrownBy(()->EquipmentRules.requireUsable("ACTIVE",LocalDate.parse("2026-10-02"),at)).hasMessageContaining("EQUIPMENT_CALIBRATION_EXPIRED");EquipmentRules.requireUsable("ACTIVE",LocalDate.parse("2026-10-03"),at);}
 @Test void inactiveAndMaintenanceEquipmentAreBlocked(){for(String s:java.util.List.of("INACTIVE","MAINTENANCE"))assertThatThrownBy(()->EquipmentRules.requireUsable(s,null,Instant.now())).hasMessageContaining("EQUIPMENT_UNAVAILABLE");}
 @Test void namedMaintenanceCommandsRejectIllegalTransitions(){assertThat(EquipmentRules.beginMaintenance("ACTIVE")).isEqualTo("MAINTENANCE");assertThat(EquipmentRules.returnToService("MAINTENANCE")).isEqualTo("ACTIVE");assertThatThrownBy(()->EquipmentRules.returnToService("INACTIVE")).hasMessageContaining("Maintenance equipment required");}
}
