package com.hospital.mes.audit.signature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.audit.domain.AuditSource;
import com.hospital.mes.audit.idempotency.IdempotencyCommand;
import com.hospital.mes.audit.idempotency.IdempotencyDecisionType;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SignatureTransactionService {
    private final SignableObjectProviderRegistry providers; private final SignatureCanonicalizer canonicalizer;
    private final SignatureRepository signatures; private final PlatformIdempotencyService idempotency;
    private final AuditApplicationService audit; private final ObjectMapper json; private final Clock clock;
    public SignatureTransactionService(SignableObjectProviderRegistry providers,SignatureCanonicalizer canonicalizer,
        SignatureRepository signatures,PlatformIdempotencyService idempotency,AuditApplicationService audit,
        ObjectMapper json,Clock clock){this.providers=providers;this.canonicalizer=canonicalizer;this.signatures=signatures;
        this.idempotency=idempotency;this.audit=audit;this.json=json;this.clock=clock;}

    @Transactional
    public SignatureResponseData signInTransaction(SignCommand c,ConsumedReauthentication reauth){
        var decision=idempotency.begin(new IdempotencyCommand(c.context().organizationId(),c.context().actorId(),
            "SIGN_RECORD",c.idempotencyKey(),c.objectType()+"|"+c.objectId()+"|"+c.meaning()+"|"+c.recordVersion()));
        if(decision.type()== IdempotencyDecisionType.REPLAY) return decode(decision.responseJson());
        if(decision.type()==IdempotencyDecisionType.CONFLICT) throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED","Idempotency key was reused");
        if(decision.type()==IdempotencyDecisionType.IN_PROGRESS_CONFLICT) throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS","Operation is in progress");
        SignableObjectProvider provider=providers.require(c.objectType(),c.meaning());
        SignableObject object=provider.loadForSignature(c.context().organizationId(),c.objectId());
        if(object.recordVersion()!=c.recordVersion()) throw new ResourceConflictException("RECORD_CHANGED","Signable record changed");
        provider.validateSignable(new SignatureValidationContext(c.context(),c.meaning()),object);
        String digest=canonicalizer.digest(object); Instant now=clock.instant();
        Long supersededSignatureId = resolveSupersededSignature(c);
        SignatureRecord record=signatures.insert(new NewSignature(c.context().organizationId(),c.context().actorId(),
            c.meaning(),c.objectType(),c.objectId(),digest,now,authContext(c,reauth),supersededSignatureId));
        String transactionId=c.context().requestId()==null?java.util.UUID.randomUUID().toString():c.context().requestId();
        audit.append(new AuditCommand(c.context().organizationId(),c.context().actorId(),c.context().roleSnapshot(),
            "SIGNATURE_APPLIED",c.objectType(),c.objectId(),null,digest,null,null,now,transactionId,
            c.context().requestId(),AuditSource.API,c.idempotencyKey()));
        SignatureResponseData response=SignatureResponseData.from(record); String body=encode(response);
        idempotency.complete(decision.handle(),200,body,"SIGNATURE",response.id());
        return response;
    }

    @Transactional(readOnly=true)
    public boolean verify(long organizationId,long signatureId){
        SignatureRecord stored=signatures.find(organizationId,signatureId);
        if(stored.status()!=SignatureStatus.VALID) return false;
        SignableObject current=providers.require(stored.objectType(),stored.meaning()).loadForSignature(organizationId,stored.objectId());
        return MessageDigest.isEqual(stored.recordDigest().getBytes(StandardCharsets.US_ASCII),
            canonicalizer.digest(current).getBytes(StandardCharsets.US_ASCII));
    }

    @Transactional
    public void invalidate(CurrentPlatformContext context, long signatureId, long expectedVersion, String reason) {
        validateInvalidationReason(reason);
        SignatureRecord existing = signatures.find(context.organizationId(), signatureId);
        invalidateRecord(context, existing, expectedVersion, reason);
    }

    @Transactional
    public int invalidateSignatures(CurrentPlatformContext context, String objectType, String objectId,
                                    String reason) {
        validateInvalidationReason(reason);
        if (objectType == null || objectType.isBlank() || objectId == null || objectId.isBlank()) {
            throw new ComplianceException(
                "SIGNATURE_OBJECT_REQUIRED", "Signature object type and identifier are required");
        }
        java.util.List<SignatureRecord> current =
            signatures.findValid(context.organizationId(), objectType, objectId);
        for (SignatureRecord signature : current) {
            invalidateRecord(context, signature, signature.versionNo(), reason);
        }
        return current.size();
    }

    private void invalidateRecord(CurrentPlatformContext context, SignatureRecord existing,
                                  long expectedVersion, String reason) {
        Instant invalidatedAt = clock.instant();
        if (!signatures.invalidate(context.organizationId(), existing.id(), expectedVersion, context.actorId(),
            invalidatedAt, reason)) {
            throw new ResourceConflictException(
                "SIGNATURE_CHANGED", "Signature is not valid at the expected version");
        }

        String transactionId = context.requestId() == null
            ? java.util.UUID.randomUUID().toString()
            : context.requestId();
        audit.append(new AuditCommand(
            context.organizationId(),
            context.actorId(),
            context.roleSnapshot(),
            "SIGNATURE_INVALIDATED",
            existing.objectType(),
            existing.objectId(),
            signatureStateDigest(existing.status(), existing.versionNo(), null, null),
            signatureStateDigest(SignatureStatus.INVALIDATED, existing.versionNo() + 1, invalidatedAt, reason),
            reason,
            null,
            invalidatedAt,
            transactionId,
            context.requestId(),
            AuditSource.API,
            null));
    }

    private static void validateInvalidationReason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new ComplianceException("INVALIDATION_REASON_REQUIRED", "Invalidation reason is required");
        }
    }

    private String authContext(SignCommand c,ConsumedReauthentication r){
        var n=json.createObjectNode();n.put("schemaVersion","1.0");n.put("method",r.method());
        n.put("reauthenticatedAt",r.reauthenticatedAt().toString());n.put("sessionIdHash",sha(c.context().sessionId()));
        if(c.context().requestId()!=null)n.put("requestId",c.context().requestId());
        try{return json.writeValueAsString(n);}catch(JsonProcessingException e){throw new IllegalStateException(e);}
    }
    private Long resolveSupersededSignature(SignCommand command) {
        if (command.revokedSignatureId() != null) {
            SignatureRecord specified = signatures.find(command.context().organizationId(), command.revokedSignatureId());
            if (specified.status() != SignatureStatus.INVALIDATED
                || specified.meaning() != command.meaning()
                || !specified.objectType().equals(command.objectType())
                || !specified.objectId().equals(command.objectId())) {
                throw new ResourceConflictException(
                    "SIGNATURE_SUPERSEDED_MISMATCH", "Superseded signature does not match the signable object");
            }
            return specified.id();
        }
        return signatures.findLatest(command.context().organizationId(), command.objectType(), command.objectId(),
                command.meaning())
            .filter(signature -> signature.status() == SignatureStatus.INVALIDATED)
            .map(SignatureRecord::id)
            .orElse(null);
    }
    private String encode(SignatureResponseData v){
        var n=json.createObjectNode();n.put("id",v.id());n.put("signerId",v.signerId());n.put("meaning",v.meaning().name());
        n.put("objectType",v.objectType());n.put("objectId",v.objectId());n.put("recordDigest",v.recordDigest());
        n.put("status",v.status().name());n.put("signedAt",v.signedAt().toString());
        if(v.revokedSignatureId()==null)n.putNull("revokedSignatureId");else n.put("revokedSignatureId",v.revokedSignatureId());
        n.put("versionNo",v.versionNo());
        try{return json.writeValueAsString(n);}catch(JsonProcessingException e){throw new IllegalStateException(e);}
    }
    private SignatureResponseData decode(String v){try{var n=json.readTree(v);return new SignatureResponseData(n.path("id").asText(),n.path("signerId").asText(),
        SignatureMeaning.valueOf(n.path("meaning").asText()),n.path("objectType").asText(),n.path("objectId").asText(),n.path("recordDigest").asText(),
        SignatureStatus.valueOf(n.path("status").asText()),Instant.parse(n.path("signedAt").asText()),n.path("revokedSignatureId").isNull()?null:n.path("revokedSignatureId").asText(),n.path("versionNo").asLong());
        }catch(Exception e){throw new IllegalStateException("Invalid replay",e);}}
    private static String signatureStateDigest(SignatureStatus status, long version, Instant invalidatedAt,
                                               String reason) {
        return sha(status.name() + "|" + version + "|"
            + (invalidatedAt == null ? "" : invalidatedAt) + "|" + (reason == null ? "" : reason));
    }
    private static String sha(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
}
