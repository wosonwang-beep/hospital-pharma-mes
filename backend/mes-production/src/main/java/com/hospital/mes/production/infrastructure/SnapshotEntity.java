package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_process_snapshot") public class SnapshotEntity extends ScopedEntity {
 private Long packageVersionId;
 public Long getPackageVersionId(){return packageVersionId;} public void setPackageVersionId(Long value){packageVersionId=value;}
 private Long formulaVersionId;
 public Long getFormulaVersionId(){return formulaVersionId;} public void setFormulaVersionId(Long value){formulaVersionId=value;}
 private Long routeVersionId;
 public Long getRouteVersionId(){return routeVersionId;} public void setRouteVersionId(Long value){routeVersionId=value;}
 private Long ebrTemplateVersionId;
 public Long getEbrTemplateVersionId(){return ebrTemplateVersionId;} public void setEbrTemplateVersionId(Long value){ebrTemplateVersionId=value;}
 private String snapshotJson;
 public String getSnapshotJson(){return snapshotJson;} public void setSnapshotJson(String value){snapshotJson=value;}
 private String snapshotHash;
 public String getSnapshotHash(){return snapshotHash;} public void setSnapshotHash(String value){snapshotHash=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
