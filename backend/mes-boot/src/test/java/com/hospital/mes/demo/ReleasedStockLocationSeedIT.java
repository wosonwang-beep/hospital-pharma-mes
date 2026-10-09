package com.hospital.mes.demo;

import static org.mockito.Mockito.when;

import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.wms.application.WmsService;
import com.hospital.mes.wms.domain.WmsCommands.InventoryMove;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
@Commit
class ReleasedStockLocationSeedIT {
    @Autowired WmsService wms;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean CurrentPlatformContextResolver contexts;

    @Test
    void moveReleasedStockFromQuarantineToReleasedAreas() {
        long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized='admin'",Long.class);
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1,actor,Set.of("SYSTEM_ADMIN"),
            Set.of("wms:inventory:view","wms:inventory:move"),
            "released-stock-location-seed","released-stock-location-seed"));

        Map<Long,Long> destination=Map.of(1L,2L,3L,4L,7L,8L);
        var rows=jdbc.queryForList("""
            SELECT l.id AS lot_id, e.location_id, e.container_id, e.unit_id,
                   SUM(e.delta_qty) AS qty
            FROM md_material_lot l
            JOIN wms_inventory_ledger e ON e.material_lot_id=l.id AND e.org_id=l.org_id
            WHERE l.org_id=1
              AND l.quality_status='RELEASED'
              AND l.inventory_status='AVAILABLE'
              AND e.location_id IN (1,3,7)
            GROUP BY l.id,e.location_id,e.container_id,e.unit_id
            HAVING SUM(e.delta_qty)>0
            ORDER BY l.id,e.location_id
            """);
        for(var row:rows){
            long source=((Number)row.get("location_id")).longValue();
            Long target=destination.get(source);
            if(target==null)continue;
            String lotId=String.valueOf(((Number)row.get("lot_id")).longValue());
            String unitId=String.valueOf(((Number)row.get("unit_id")).longValue());
            String containerId=row.get("container_id")==null?null:String.valueOf(((Number)row.get("container_id")).longValue());
            BigDecimal qty=(BigDecimal)row.get("qty");
            var lot=wms.get("MaterialLot",lotId);
            wms.move(new InventoryMove(
                lotId,
                Long.toString(source),containerId,
                Long.toString(target),containerId,
                qty.stripTrailingZeros().toPlainString(),
                unitId,
                lot.path("versionNo").asLong(),
                "质量放行后由仓储从待验区转移至合格/放行区"),
                quote(lot.path("versionNo").asLong()),
                UUID.randomUUID().toString());
        }
    }

    private String quote(long version){return "\""+version+"\"";}
}
