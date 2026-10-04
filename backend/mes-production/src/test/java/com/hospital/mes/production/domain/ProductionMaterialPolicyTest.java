package com.hospital.mes.production.domain;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.production.application.ProductionMaterialPolicyService;
import org.springframework.mock.env.MockEnvironment;
import org.junit.jupiter.api.Test;

class ProductionMaterialPolicyTest {
 private final ObjectMapper json=new ObjectMapper();
 private final MasterMutation digest=new MasterMutation(null,null,null,json);
 private MockEnvironment configured(){String p="mes.production.material-weighing-policies[0].";return new MockEnvironment().withProperty(p+"organization-id","1").withProperty(p+"material-id","25").withProperty(p+"required","true").withProperty(p+"precision","0.010").withProperty(p+"tolerance-pct","0.5").withProperty(p+"policy-version","CONTROLLED-1");}
 @Test void missingPolicyAndCrossOrganizationFailClosed(){var service=new ProductionMaterialPolicyService(new MockEnvironment(),digest,json);assertThatThrownBy(()->service.requirePolicy(1,25,3)).hasMessageContaining("25");var configured=new ProductionMaterialPolicyService(configured(),digest,json);assertThatThrownBy(()->configured.requirePolicy(2,25,3)).hasMessageContaining("25");}
 @Test void frozenPolicyValidatesDigestAndNeverReReadsConfiguration(){var env=configured();var service=new ProductionMaterialPolicyService(env,digest,json);var snapshot=service.requirePolicy(1,25,3);assertThat(snapshot.path("required").booleanValue()).isTrue();assertThat(snapshot.path("precision").asText()).isEqualTo("0.01");env.setProperty("mes.production.material-weighing-policies[0].tolerance-pct","8");assertThat(service.requireFrozen(snapshot,1,25,3).path("tolerancePct").asText()).isEqualTo("0.5");((com.fasterxml.jackson.databind.node.ObjectNode)snapshot).put("required",false);assertThatThrownBy(()->service.requireFrozen(snapshot,1,25,3)).hasMessageContaining("policy");}
 @Test void negativeAndUnspecifiedRequiredNeverBecomePermissive(){var env=configured().withProperty("mes.production.material-weighing-policies[0].precision","-1");assertThatThrownBy(()->new ProductionMaterialPolicyService(env,digest,json).requirePolicy(1,25,3)).hasMessageContaining("25");var blank=configured().withProperty("mes.production.material-weighing-policies[0].required","");assertThatThrownBy(()->new ProductionMaterialPolicyService(blank,digest,json).requirePolicy(1,25,3)).hasMessageContaining("25");}
}
