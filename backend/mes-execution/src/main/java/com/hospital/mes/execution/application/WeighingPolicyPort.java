package com.hospital.mes.execution.application;
import java.util.Set;
/** Explicit governed deployment binding. No permission/critical-material inference. */
public interface WeighingPolicyPort {
 record Policy(boolean independentVerificationRequired,boolean signatureRequired,Set<String> scaleEquipmentTypes,String configurationHash){}
 Policy requirePolicy(long organizationId,long materialId);
}
