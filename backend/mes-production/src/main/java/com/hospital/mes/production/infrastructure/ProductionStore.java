package com.hospital.mes.production.infrastructure;
import com.hospital.mes.masterdata.application.ScopedStore;
import java.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionStore {
 public final OrderMapper orderMapper; public final BatchMapper batchMapper; public final SubBatchMapper subMapper; public final ExecutionUnitMapper executionMapper; public final SnapshotMapper snapshotMapper;
 public final ScopedStore<OrderEntity> orders; public final ScopedStore<BatchEntity> batches; public final ScopedStore<SubBatchEntity> subs; public final ScopedStore<ExecutionUnitEntity> executions; public final ScopedStore<SnapshotEntity> snapshots;
 public ProductionStore(OrderMapper o,BatchMapper b,SubBatchMapper s,ExecutionUnitMapper e,SnapshotMapper p){
  orderMapper=o;batchMapper=b;subMapper=s;executionMapper=e;snapshotMapper=p;
  orders=new ScopedStore<>(o,List.of("order_no"),Map.of("status","status","productId","product_id"));
  batches=new ScopedStore<>(b,List.of("batch_no"),Map.of("status","status","productId","product_id","productionOrderId","production_order_id"));
  subs=new ScopedStore<>(s,List.of(),Map.of());executions=new ScopedStore<>(e,List.of(),Map.of());snapshots=new ScopedStore<>(p,List.of(),Map.of());
 }
 public List<SubBatchEntity> subs(long org,long batch){return subMapper.selectList(new QueryWrapper<SubBatchEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("sequence_no"));}
 public List<ExecutionUnitEntity> executions(long org,long batch){return executionMapper.selectList(new QueryWrapper<ExecutionUnitEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("id"));}
 public List<BatchEntity> batches(long org,long order){return batchMapper.selectList(new QueryWrapper<BatchEntity>().eq("org_id",org).eq("production_order_id",order).orderByAsc("id"));}
 /** Caller holds the order lock. Current read must see allocations committed while it was waiting. */
 public List<BatchEntity> lockedAllocations(long org,long order){return batchMapper.selectList(new QueryWrapper<BatchEntity>().eq("org_id",org).eq("production_order_id",order).orderByAsc("id").last("FOR UPDATE"));}
}
