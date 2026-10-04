package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_request_item")
public class InspectionRequestItemRow extends IncomingRow {
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Integer itemNo;
 public Integer getItemNo(){return itemNo;} public void setItemNo(Integer value){itemNo=value;}
}
