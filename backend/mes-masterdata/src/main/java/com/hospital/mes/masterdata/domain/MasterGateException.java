package com.hospital.mes.masterdata.domain;
import com.hospital.mes.common.exception.ComplianceException;
import java.util.List;
public class MasterGateException extends ComplianceException {
    private final List<String> requiredActions;
    public MasterGateException(String code, String action) { super(code, code); requiredActions=List.of(action); }
    public List<String> requiredActions() { return requiredActions; }
}
