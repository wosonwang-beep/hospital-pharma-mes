package com.hospital.mes.audit.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.idempotency.IdempotencyDecision;
import com.hospital.mes.audit.idempotency.IdempotencyHandle;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SignatureTransactionServiceTest {
    @Test
    void createsServerDigestSignatureAuditAndIdempotencyCompletion() throws Exception {
        ObjectMapper json = new ObjectMapper();
        SignableObject object = new SignableObject("TEST_RECORD", "42", 3, json.readTree("{\"value\":\"1.20\"}"), List.of());
        SignableObjectProvider provider = mock(SignableObjectProvider.class);
        when(provider.objectType()).thenReturn("TEST_RECORD");
        when(provider.allowedMeanings()).thenReturn(Set.of(SignatureMeaning.VERIFY));
        when(provider.loadForSignature(11, "42")).thenReturn(object);
        SignatureRepository signatures = mock(SignatureRepository.class);
        when(signatures.insert(any())).thenAnswer(i -> ((NewSignature)i.getArgument(0)).toRecord(99));
        PlatformIdempotencyService idempotency = mock(PlatformIdempotencyService.class);
        when(idempotency.begin(any())).thenReturn(IdempotencyDecision.owner(new IdempotencyHandle(1, 0)));
        AuditApplicationService audit = mock(AuditApplicationService.class);
        SignatureTransactionService service = new SignatureTransactionService(
            new SignableObjectProviderRegistry(List.of(provider)), new Rfc8785SignatureCanonicalizer(json),
            signatures, idempotency, audit, json, Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(11, 7, Set.of("QA"), Set.of("ebr:sign"), "s1", "r1");
        SignatureResponseData result = service.signInTransaction(new SignCommand(context, "TEST_RECORD", "42",
            SignatureMeaning.VERIFY, 3, "unused-after-consume", "idem-1", null),
            new ConsumedReauthentication(Instant.parse("2026-09-29T00:00:00Z"), "PASSWORD"));
        assertThat(result.id()).isEqualTo("99");
        assertThat(result.recordDigest()).matches("[0-9a-f]{64}");
        verify(audit).append(any());
        verify(idempotency).complete(any(), org.mockito.ArgumentMatchers.eq(200), any(),
            org.mockito.ArgumentMatchers.eq("SIGNATURE"), org.mockito.ArgumentMatchers.eq("99"));
    }
}
