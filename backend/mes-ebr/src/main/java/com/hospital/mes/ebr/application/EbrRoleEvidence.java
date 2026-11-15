package com.hospital.mes.ebr.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.SignatureRecord;
import com.hospital.mes.audit.signature.SignatureStatus;
import com.hospital.mes.ebr.domain.EbrCommands.SignatureRule;
import org.springframework.stereotype.Service;

/** Frozen EBR role policy is carried by the signature itself, not by an audit trail. */
@Service
public class EbrRoleEvidence {
    private final ObjectMapper json;
    public EbrRoleEvidence(ObjectMapper json) { this.json = json; }

    public static String policy(SignatureRule rule) {
        String key = rule.meaning()+"|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.sequenceNo();
        if (rule.requiredRoleId() == null) return key;
        return "2.0|"+rule.requiredRoleId()+"|"+key+"|"
            +com.hospital.mes.audit.idempotency.PlatformIdempotencyService.digest(
                rule.requiredRoleId()+"|"+key+"|"+rule.roleCodeSnapshot()+"|"+rule.roleNameSnapshot()
                +"|"+rule.reauthRequired()+"|"+rule.invalidateOnChange());
    }

    public void retain(CurrentPlatformContext context, SignatureRecord signature, SignatureRule rule) {
        if (signature.organizationId()!=context.organizationId() || signature.signerId()!=context.actorId()
            || !matches(signature,rule)) {
            throw new IllegalArgumentException("Signature role policy binding mismatch");
        }
    }

    public boolean matches(SignatureRecord signature, SignatureRule rule) {
        if (signature.status()!=SignatureStatus.VALID || !signature.meaning().name().equals(rule.meaning())) return false;
        if (signature.authContextJson()==null || signature.authContextJson().isBlank()) return false;
        try {
            var auth=json.readTree(signature.authContextJson());
            var policies=auth.path("providerContext").path("rolePolicies");
            if (!policies.isArray()) return false;
            for (var item:policies) if (policy(rule).equals(item.asText())) return true;
            return false;
        } catch (Exception ex) { return false; }
    }
}
