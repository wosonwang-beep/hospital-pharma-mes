package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_material_issue_item")
public class MaterialIssueItemEntity extends ScopedEntity {
 private Long materialRequestItemId;
 public Long getMaterialRequestItemId(){return materialRequestItemId;} public void setMaterialRequestItemId(Long value){materialRequestItemId=value;}
 private Long issueId;
 public Long getIssueId(){return issueId;} public void setIssueId(Long value){issueId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long formulaItemId;
 public Long getFormulaItemId(){return formulaItemId;} public void setFormulaItemId(Long value){formulaItemId=value;}
 private BigDecimal issuedQty;
 public BigDecimal getIssuedQty(){return issuedQty;} public void setIssuedQty(BigDecimal value){issuedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
}
