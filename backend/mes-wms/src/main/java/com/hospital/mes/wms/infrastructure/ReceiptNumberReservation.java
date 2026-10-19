package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_receipt_number_reservation")
public class ReceiptNumberReservation extends ScopedEntity {
 private Long ruleId;
 public Long getRuleId(){return ruleId;} public void setRuleId(Long v){ruleId=v;}
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
 private String clientLineId;
 public String getClientLineId(){return clientLineId;} public void setClientLineId(String v){clientLineId=v;}
 private String lotNo;
 public String getLotNo(){return lotNo;} public void setLotNo(String v){lotNo=v;}
 private String approvalReference;
 public String getApprovalReference(){return approvalReference;} public void setApprovalReference(String v){approvalReference=v;}
 private Long boundReceiptItemId;
 public Long getBoundReceiptItemId(){return boundReceiptItemId;} public void setBoundReceiptItemId(Long v){boundReceiptItemId=v;}
}
