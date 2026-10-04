package com.hospital.mes.wms.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import java.util.*;
@org.springframework.stereotype.Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsStore {
 private final WarehouseMapper warehouseMapper;
 private final ScopedStore<WarehouseEntity> warehouse;
 private final LocationMapper locationMapper;
 private final ScopedStore<LocationEntity> location;
 private final ContainerMapper containerMapper;
 private final ScopedStore<ContainerEntity> container;
 private final ReceiptMapper receiptMapper;
 private final ScopedStore<ReceiptEntity> receipt;
 private final ReceiptItemMapper receiptItemMapper;
 private final ScopedStore<ReceiptItemEntity> receiptItem;
 private final MaterialLotMapper materialLotMapper;
 private final ScopedStore<MaterialLotEntity> materialLot;
 private final LedgerMapper ledgerMapper;
 private final ScopedStore<LedgerEntity> ledger;
 private final ReservationMapper reservationMapper;
 private final ScopedStore<ReservationEntity> reservation;
 private final MaterialIssueMapper materialIssueMapper;
 private final ScopedStore<MaterialIssueEntity> materialIssue;
 private final MaterialIssueItemMapper materialIssueItemMapper;
 private final ScopedStore<MaterialIssueItemEntity> materialIssueItem;
 public WmsStore(WarehouseMapper warehouseMapper,LocationMapper locationMapper,ContainerMapper containerMapper,ReceiptMapper receiptMapper,ReceiptItemMapper receiptItemMapper,MaterialLotMapper materialLotMapper,LedgerMapper ledgerMapper,ReservationMapper reservationMapper,MaterialIssueMapper materialIssueMapper,MaterialIssueItemMapper materialIssueItemMapper){
 this.warehouseMapper=warehouseMapper; this.warehouse=new ScopedStore<>(warehouseMapper,List.of("warehouse_code","warehouse_name"),Map.of("status","status"));
 this.locationMapper=locationMapper; this.location=new ScopedStore<>(locationMapper,List.of("location_code","location_name"),Map.of("warehouseId","warehouse_id","status","status"));
 this.containerMapper=containerMapper; this.container=new ScopedStore<>(containerMapper,List.of("container_code"),Map.of("status","status"));
 this.receiptMapper=receiptMapper; this.receipt=new ScopedStore<>(receiptMapper,List.of("receipt_no"),Map.of("recordStatus","record_status","supplierId","supplier_id","warehouseId","warehouse_id"));
 this.receiptItemMapper=receiptItemMapper; this.receiptItem=new ScopedStore<>(receiptItemMapper,List.of(),Map.of());
 this.materialLotMapper=materialLotMapper; this.materialLot=new ScopedStore<>(materialLotMapper,List.of("lot_no"),Map.of("materialId","material_id","qualityStatus","quality_status","inventoryStatus","inventory_status"));
 this.ledgerMapper=ledgerMapper; this.ledger=new ScopedStore<>(ledgerMapper,List.of(),Map.of());
 this.reservationMapper=reservationMapper; this.reservation=new ScopedStore<>(reservationMapper,List.of(),Map.of());
 this.materialIssueMapper=materialIssueMapper; this.materialIssue=new ScopedStore<>(materialIssueMapper,List.of("issue_no"),Map.of("mainBatchId","main_batch_id","status","status"));
 this.materialIssueItemMapper=materialIssueItemMapper; this.materialIssueItem=new ScopedStore<>(materialIssueItemMapper,List.of(),Map.of());
 }
 public WarehouseMapper warehouseMapper(){return warehouseMapper;}
 public ScopedStore<WarehouseEntity> warehouse(){return warehouse;}
 public LocationMapper locationMapper(){return locationMapper;}
 public ScopedStore<LocationEntity> location(){return location;}
 public ContainerMapper containerMapper(){return containerMapper;}
 public ScopedStore<ContainerEntity> container(){return container;}
 public ReceiptMapper receiptMapper(){return receiptMapper;}
 public ScopedStore<ReceiptEntity> receipt(){return receipt;}
 public ReceiptItemMapper receiptItemMapper(){return receiptItemMapper;}
 public ScopedStore<ReceiptItemEntity> receiptItem(){return receiptItem;}
 public MaterialLotMapper materialLotMapper(){return materialLotMapper;}
 public ScopedStore<MaterialLotEntity> materialLot(){return materialLot;}
 public LedgerMapper ledgerMapper(){return ledgerMapper;}
 public ScopedStore<LedgerEntity> ledger(){return ledger;}
 public ReservationMapper reservationMapper(){return reservationMapper;}
 public ScopedStore<ReservationEntity> reservation(){return reservation;}
 public MaterialIssueMapper materialIssueMapper(){return materialIssueMapper;}
 public ScopedStore<MaterialIssueEntity> materialIssue(){return materialIssue;}
 public MaterialIssueItemMapper materialIssueItemMapper(){return materialIssueItemMapper;}
 public ScopedStore<MaterialIssueItemEntity> materialIssueItem(){return materialIssueItem;}
}
