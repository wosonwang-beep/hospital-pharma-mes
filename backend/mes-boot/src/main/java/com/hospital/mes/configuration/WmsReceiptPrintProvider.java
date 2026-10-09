package com.hospital.mes.configuration;

import com.hospital.mes.reporting.application.*;
import com.hospital.mes.wms.application.WmsService;
import com.hospital.mes.wms.infrastructure.WmsStore;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.infrastructure.*;
import com.hospital.mes.system.infrastructure.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;

/** Read adapter over authorized receipt facts. Confirmation snapshots take precedence. */
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WmsReceiptPrintProvider implements PrintDataProvider {
 private final WmsService wms;private final WmsStore db;private final MasterMutation mutations;
 private final SupplierMapper suppliers;private final UnitMapper units;private final SysUserMapper users;
 public WmsReceiptPrintProvider(WmsService wms,WmsStore db,MasterMutation mutations,SupplierMapper suppliers,UnitMapper units,SysUserMapper users){this.wms=wms;this.db=db;this.mutations=mutations;this.suppliers=suppliers;this.units=units;this.users=users;}
 public String businessType(){return "WMS_RECEIPT";}public String moduleLabel(){return "WMS管理";}public String documentLabel(){return "收货单";}
 public List<PrintField> fieldDefinitions(){return List.of(
  field("reportNo","收货单号","单据基本信息",false),field("modeLabel","打印标识","打印信息",false),field("receiptNo","收货编号","单据基本信息",false),field("receivedAt","收货日期","单据基本信息",false),field("recordStatus","记录状态","单据基本信息",false),
  field("purchaseOrderNo","采购订单号","单据基本信息",false),field("deliveryNoteNo","送货单号","单据基本信息",false),field("warehouseName","收货仓库","单据基本信息",false),field("receivedBy","收货人","单据基本信息",false),field("confirmedBy","确认人","单据基本信息",false),field("confirmedAt","确认日期","单据基本信息",false),
  field("supplierCode","供应商编码","关联供应商",false),field("supplierName","供应商名称","关联供应商",false),field("materialNames","物料名称汇总","单据基本信息",false),
  field("sequence","序号","收货明细",true),field("materialCode","物料编码","收货明细",true),field("materialName","物料名称","收货明细",true),field("packageSpec","包装规格","收货明细",true),field("supplierLotNo","供应商批号","收货明细",true),field("lotNo","内部批号","收货明细",true),
  new PrintField("receivedQty","收货数量","收货明细","收货记录中保存的实际数量","100",true,"NUMBER","unit"),field("unit","单位","收货明细",true),field("packageCount","包装件数","收货明细",true),field("manufacturerLotNo","制造商批号","收货明细",true),field("manufactureDate","生产日期","收货明细",true),field("expiryDate","有效期","收货明细",true),field("retestDate","复验日期","收货明细",true));}
 private PrintField field(String key,String label,String group,boolean repeated){return new PrintField(key,label,group,"收货记录实际保存的数据",label+"示例",repeated);}
 public Set<String> fields(){var set=new HashSet<String>();fieldDefinitions().stream().filter(f->!f.repeated()).forEach(f->set.add(f.key()));set.add("items");return Set.copyOf(set);}
 public Set<String> itemFields(){var set=new HashSet<String>();fieldDefinitions().stream().filter(PrintField::repeated).forEach(f->set.add(f.key()));return Set.copyOf(set);}
 public Map<String,Object> example(){var data=new LinkedHashMap<String,Object>();fieldDefinitions().stream().filter(f->!f.repeated()).forEach(f->data.put(f.key(),f.example()));data.put("reportNo","REC-EXAMPLE-001");data.put("receiptNo","REC-EXAMPLE-001");data.put("modeLabel","收货记录示例");var item=new LinkedHashMap<String,Object>();fieldDefinitions().stream().filter(PrintField::repeated).forEach(f->item.put(f.key(),f.example()));item.put("receivedQty",100);item.put("unit","kg");data.put("items",List.of(item));return Map.copyOf(data);}
 public void authorizeRead(String id){wms.get("Receipt",id);}
 @Transactional public PrintSnapshot load(String id,boolean formal){
  if(formal)throw new IllegalArgumentException("收货单按业务记录打印，不作为质量正式签署报告");
  var c=mutations.context("wms:receipt:view");var row=db.receipt().lock(c.organizationId(),MasterMutation.id(id));var receipt=wms.get("Receipt",id);
  var supplier=suppliers.selectOne(new QueryWrapper<SupplierEntity>().eq("org_id",c.organizationId()).eq("id",row.getSupplierId()));
  var warehouse=db.warehouse().get(c.organizationId(),row.getWarehouseId());
  var data=new LinkedHashMap<String,Object>();for(var key:List.of("receiptNo","receivedAt","purchaseOrderNo","deliveryNoteNo","confirmedAt"))data.put(key,text(receipt,key));
  data.put("reportNo",row.getReceiptNo());data.put("modeLabel","APPROVED".equals(row.getRecordStatus())?"已确认收货记录":"草稿收货记录");data.put("recordStatus","APPROVED".equals(row.getRecordStatus())?"已确认":"草稿");
  data.put("warehouseName",Objects.toString(warehouse.getWarehouseName(),"仓库 #"+warehouse.getId()));data.put("receivedBy",name(row.getReceivedBy()));data.put("confirmedBy",name(row.getConfirmedBy()));
  data.put("supplierName",supplier==null?"供应商 #"+row.getSupplierId():supplier.getSupplierName());data.put("supplierCode",supplier==null?"":supplier.getSupplierCode());
  var items=new ArrayList<Map<String,Object>>();var names=new LinkedHashSet<String>();int sequence=0;
  for(var source:receipt.path("items")){
   var material=source.path("materialSnapshot");var item=new LinkedHashMap<String,Object>();item.put("sequence",++sequence);
   for(var key:List.of("materialCode","materialName"))item.put(key,text(material,key));names.add(text(material,"materialName"));
   for(var key:List.of("packageSpec","supplierLotNo","lotNo","receivedQty","packageCount","manufacturerLotNo","manufactureDate","expiryDate","retestDate"))item.put(key,text(source,key));
   var unit=units.selectOne(new QueryWrapper<UnitEntity>().eq("org_id",c.organizationId()).eq("id",MasterMutation.id(source.path("unitId").asText())));item.put("unit",unit==null?"单位 #"+source.path("unitId").asText():unit.getUnitName());
   var snapshot=source.path("sourceSnapshot");for(var key:List.of("supplierName","supplierCode"))if(snapshot.hasNonNull(key))data.put(key,text(snapshot,key));items.add(item);
  }
  data.put("materialNames",String.join("、",names));data.put("items",List.copyOf(items));return new PrintSnapshot(Long.toString(row.getVersionNo()),row.getReceiptNo(),Map.copyOf(data));
 }
 private String name(Long id){if(id==null)return "";var user=users.selectById(id);return user==null?"人员 #"+id:user.getDisplayName();}
 private String text(JsonNode node,String key){return node.path(key).isNull()?"":node.path(key).asText("");}
}
