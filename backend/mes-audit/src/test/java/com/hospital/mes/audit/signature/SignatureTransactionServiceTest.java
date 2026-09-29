package com.hospital.mes.audit.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

    @Test
    void resignLinksTheImmediatelySupersededInvalidatedSignature() throws Exception {
        ObjectMapper json = new ObjectMapper();
        SignableObject object = new SignableObject(
            "TEST_RECORD", "42", 4, json.readTree("{\"value\":\"corrected\"}"), List.of());
        SignableObjectProvider provider = mock(SignableObjectProvider.class);
        when(provider.objectType()).thenReturn("TEST_RECORD");
        when(provider.allowedMeanings()).thenReturn(Set.of(SignatureMeaning.VERIFY));
        when(provider.loadForSignature(11, "42")).thenReturn(object);
        SignatureRepository signatures = mock(SignatureRepository.class);
        SignatureRecord invalidated = new SignatureRecord(88, 11, 5, SignatureMeaning.VERIFY,
            "TEST_RECORD", "42", "b".repeat(64), Instant.parse("2026-09-28T00:00:00Z"),
            SignatureStatus.INVALIDATED, Instant.parse("2026-09-28T01:00:00Z"), "correction", "{}", null, 1);
        when(signatures.findLatest(11, "TEST_RECORD", "42", SignatureMeaning.VERIFY))
            .thenReturn(Optional.of(invalidated));
        when(signatures.insert(any())).thenAnswer(i -> ((NewSignature)i.getArgument(0)).toRecord(99));
        PlatformIdempotencyService idempotency = mock(PlatformIdempotencyService.class);
        when(idempotency.begin(any())).thenReturn(IdempotencyDecision.owner(new IdempotencyHandle(1, 0)));
        SignatureTransactionService service = new SignatureTransactionService(
            new SignableObjectProviderRegistry(List.of(provider)), new Rfc8785SignatureCanonicalizer(json),
            signatures, idempotency, mock(AuditApplicationService.class), json,
            Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(
            11, 7, Set.of("QA"), Set.of("ebr:sign"), "s1", "r1");

        SignatureResponseData result = service.signInTransaction(new SignCommand(context, "TEST_RECORD", "42",
            SignatureMeaning.VERIFY, 4, "unused-after-consume", "idem-2", null),
            new ConsumedReauthentication(Instant.parse("2026-09-29T00:00:00Z"), "PASSWORD"));

        assertThat(result.revokedSignatureId()).isEqualTo("88");
    }

    @Test
    void invalidatesSignatureAndAppendsAuditInTheSameServiceTransaction() {
        ObjectMapper json = new ObjectMapper();
        SignatureRepository signatures = mock(SignatureRepository.class);
        SignatureRecord existing = new SignatureRecord(99, 11, 5, SignatureMeaning.VERIFY,
            "TEST_RECORD", "42", "a".repeat(64), Instant.parse("2026-09-28T00:00:00Z"),
            SignatureStatus.VALID, null, null, "{}", null, 3);
        when(signatures.find(11, 99)).thenReturn(existing);
        when(signatures.invalidate(11, 99, 3, 7, Instant.parse("2026-09-29T00:00:00Z"),
            "corrected evidence")).thenReturn(true);
        AuditApplicationService audit = mock(AuditApplicationService.class);
        SignatureTransactionService service = new SignatureTransactionService(
            new SignableObjectProviderRegistry(List.of()), mock(SignatureCanonicalizer.class),
            signatures, mock(PlatformIdempotencyService.class), audit, json,
            Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(
            11, 7, Set.of("QA"), Set.of("ebr:sign"), "session-1", "request-1");

        service.invalidate(context, 99, 3, "corrected evidence");

        ArgumentCaptor<com.hospital.mes.audit.domain.AuditCommand> event =
            ArgumentCaptor.forClass(com.hospital.mes.audit.domain.AuditCommand.class);
        verify(audit).append(event.capture());
        assertThat(event.getValue().action()).isEqualTo("SIGNATURE_INVALIDATED");
        assertThat(event.getValue().objectType()).isEqualTo("TEST_RECORD");
        assertThat(event.getValue().objectId()).isEqualTo("42");
        assertThat(event.getValue().reason()).isEqualTo("corrected evidence");
        assertThat(event.getValue().actorRole()).isEqualTo("QA");
        assertThat(event.getValue().requestId()).isEqualTo("request-1");
        assertThat(event.getValue().oldValueDigest()).isNotEqualTo(event.getValue().newValueDigest());
    }

    @Test
    void invalidatesEveryCurrentSignatureForAnAffectedObject() {
        ObjectMapper json = new ObjectMapper();
        SignatureRepository signatures = mock(SignatureRepository.class);
        SignatureRecord first = new SignatureRecord(99, 11, 5, SignatureMeaning.VERIFY,
            "TEST_RECORD", "42", "a".repeat(64), Instant.parse("2026-09-28T00:00:00Z"),
            SignatureStatus.VALID, null, null, "{}", null, 3);
        SignatureRecord second = new SignatureRecord(100, 11, 6, SignatureMeaning.APPROVE,
            "TEST_RECORD", "42", "b".repeat(64), Instant.parse("2026-09-28T01:00:00Z"),
            SignatureStatus.VALID, null, null, "{}", null, 1);
        when(signatures.findValid(11, "TEST_RECORD", "42")).thenReturn(List.of(first, second));
        when(signatures.invalidate(any(Long.class), any(Long.class), any(Long.class), any(Long.class),
            any(Instant.class), any(String.class))).thenReturn(true);
        AuditApplicationService audit = mock(AuditApplicationService.class);
        SignatureTransactionService service = new SignatureTransactionService(
            new SignableObjectProviderRegistry(List.of()), mock(SignatureCanonicalizer.class),
            signatures, mock(PlatformIdempotencyService.class), audit, json,
            Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));
        CurrentPlatformContext context = new CurrentPlatformContext(
            11, 7, Set.of("QA"), Set.of("ebr:sign"), "session-1", "request-1");

        int count = service.invalidateSignatures(context, "TEST_RECORD", "42", "corrected evidence");

        assertThat(count).isEqualTo(2);
        verify(signatures, times(2)).invalidate(any(Long.class), any(Long.class), any(Long.class),
            any(Long.class), any(Instant.class), any(String.class));
        verify(audit, times(2)).append(any());
    }
}
