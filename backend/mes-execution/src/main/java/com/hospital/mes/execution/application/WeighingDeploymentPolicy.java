package com.hospital.mes.execution.application;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.common.exception.ComplianceException;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.core.env.Environment;
import java.util.*;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WeighingDeploymentPolicy implements WeighingPolicyPort {
 private final Environment environment;private final MasterMutation mutations;
 public WeighingDeploymentPolicy(Environment environment,MasterMutation mutations){this.environment=environment;this.mutations=mutations;}
 public Policy requirePolicy(long organizationId,long materialId){var types=Binder.get(environment).bind("mes.weighing.scale-equipment-types",Bindable.listOf(String.class)).orElse(List.of());if(types.isEmpty()||types.stream().anyMatch(x->x==null||x.isBlank()))throw new ComplianceException("WEIGHING_POLICY_REQUIRED","Explicit scale equipment type deployment binding required");var selected=Collections.unmodifiableSet(new TreeSet<>(types));String hash=mutations.digest(Map.of("policyVersion","INCOMING-WEIGH-1","independentVerificationRequired",true,"signatureRequired",true,"scaleEquipmentTypes",selected));return new Policy(true,true,selected,hash);}
}
