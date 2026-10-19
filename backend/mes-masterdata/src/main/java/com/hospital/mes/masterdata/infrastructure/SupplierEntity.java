package com.hospital.mes.masterdata.infrastructure;

import com.hospital.mes.masterdata.domain.SupplierRules;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@com.baomidou.mybatisplus.annotation.TableName("md_supplier")
public class SupplierEntity extends ScopedEntity {
    private String supplierCode;
    private String supplierName;
    private String qualificationStatus;
    private Boolean enabled;
    private LocalDate validTo;

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String value) { supplierCode = value; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String value) { supplierName = value; }
    public String getQualificationStatus() { return qualificationStatus; }
    public void setQualificationStatus(String value) { qualificationStatus = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { enabled = value; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate value) { validTo = value; }

    @Override public List<String> allowedActions() {
        if (!Boolean.TRUE.equals(enabled)) return List.of("ENABLE");
        var actions = new ArrayList<String>(List.of("UPDATE", "DISABLE"));
        if (SupplierRules.PENDING.equals(qualificationStatus)) {
            actions.add("QUALIFY");
            actions.add("REJECT");
        } else if (SupplierRules.QUALIFIED.equals(qualificationStatus)) {
            actions.add("REVIEW");
            actions.add("REJECT");
        } else if (SupplierRules.UNQUALIFIED.equals(qualificationStatus)) {
            actions.add("REVIEW");
        }
        return List.copyOf(actions);
    }
}
