package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("qms_production_test_instance")
public class ProductionTestInstanceRow extends MutableIncomingRow {
    private Long sampleId;
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long value) { sampleId = value; }
    private Long productionPlanId;
    public Long getProductionPlanId() { return productionPlanId; }
    public void setProductionPlanId(Long value) { productionPlanId = value; }
    private Long qcSpecificationItemId;
    public Long getQcSpecificationItemId() { return qcSpecificationItemId; }
    public void setQcSpecificationItemId(Long value) { qcSpecificationItemId = value; }
    private String specificationSnapshotJson;
    public String getSpecificationSnapshotJson() { return specificationSnapshotJson; }
    public void setSpecificationSnapshotJson(String value) { specificationSnapshotJson = value; }
    private String testCode;
    public String getTestCode() { return testCode; }
    public void setTestCode(String value) { testCode = value; }
    private Integer attemptNo;
    public Integer getAttemptNo() { return attemptNo; }
    public void setAttemptNo(Integer value) { attemptNo = value; }
    private Long previousInstanceId;
    public Long getPreviousInstanceId() { return previousInstanceId; }
    public void setPreviousInstanceId(Long value) { previousInstanceId = value; }
    private Long approvedInvestigationId;
    public Long getApprovedInvestigationId() { return approvedInvestigationId; }
    public void setApprovedInvestigationId(Long value) { approvedInvestigationId = value; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    private Long currentResultRevisionId;
    public Long getCurrentResultRevisionId() { return currentResultRevisionId; }
    public void setCurrentResultRevisionId(Long value) { currentResultRevisionId = value; }
    private Long instrumentId;
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long value) { instrumentId = value; }
    private Long performedBy;
    public Long getPerformedBy() { return performedBy; }
    public void setPerformedBy(Long value) { performedBy = value; }
}
