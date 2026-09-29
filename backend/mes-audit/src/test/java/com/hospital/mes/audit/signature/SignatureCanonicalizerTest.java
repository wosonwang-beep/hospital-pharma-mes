package com.hospital.mes.audit.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SignatureCanonicalizerTest {
    private final ObjectMapper json = new ObjectMapper();
    private final Rfc8785SignatureCanonicalizer canonicalizer = new Rfc8785SignatureCanonicalizer(json);

    @Test
    void canonicalizesPropertyOrderAndSortsDeduplicatesEvidence() throws Exception {
        SignableObject one = new SignableObject("EBR_FORM_INSTANCE", "123", 7,
            json.readTree("{\"z\":2,\"a\":\"1.20\"}"), List.of("FIELD_REVISION:81", "ATTACHMENT:9", "ATTACHMENT:9"));
        SignableObject two = new SignableObject("EBR_FORM_INSTANCE", "123", 7,
            json.readTree("{\"a\":\"1.20\",\"z\":2}"), List.of("ATTACHMENT:9", "FIELD_REVISION:81"));
        assertThat(canonicalizer.canonicalJson(one)).isEqualTo(canonicalizer.canonicalJson(two));
        assertThat(canonicalizer.canonicalJson(one)).contains("\"evidenceIds\":[\"ATTACHMENT:9\",\"FIELD_REVISION:81\"]");
        assertThat(canonicalizer.digest(one)).matches("[0-9a-f]{64}").isEqualTo(canonicalizer.digest(two));
    }

    @Test
    void rejectsBinaryFloatingPointInSignedRecord() throws Exception {
        SignableObject object = new SignableObject("EBR_FORM_INSTANCE", "123", 7,
            json.readTree("{\"amount\":1.2}"), List.of());
        assertThatThrownBy(() -> canonicalizer.digest(object)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerRegistryIsClosedByTypeAndMeaning() {
        SignableObjectProvider provider = new SignableObjectProvider() {
            public String objectType() { return "TEST_RECORD"; }
            public Set<SignatureMeaning> allowedMeanings() { return Set.of(SignatureMeaning.VERIFY); }
            public SignableObject loadForSignature(long org, String id) { return null; }
            public void validateSignable(SignatureValidationContext context, SignableObject object) { }
        };
        SignableObjectProviderRegistry registry = new SignableObjectProviderRegistry(List.of(provider));
        assertThat(registry.require("TEST_RECORD", SignatureMeaning.VERIFY)).isSameAs(provider);
        assertThatThrownBy(() -> registry.require("UNKNOWN", SignatureMeaning.VERIFY))
            .isInstanceOf(UnsupportedSignableObjectException.class);
        assertThatThrownBy(() -> registry.require("TEST_RECORD", SignatureMeaning.APPROVE))
            .isInstanceOf(SignatureMeaningNotAllowedException.class);
    }
}
