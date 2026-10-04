package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_genealogy") public class GenealogyEntity extends ScopedEntity {
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long inputMaterialLotId;
 public Long getInputMaterialLotId(){return inputMaterialLotId;} public void setInputMaterialLotId(Long value){inputMaterialLotId=value;}
 private Long chargeId;
 public Long getChargeId(){return chargeId;} public void setChargeId(Long value){chargeId=value;}
 private Long outputLotId;
 public Long getOutputLotId(){return outputLotId;} public void setOutputLotId(Long value){outputLotId=value;}
 private String relationType;
 public String getRelationType(){return relationType;} public void setRelationType(String value){relationType=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
