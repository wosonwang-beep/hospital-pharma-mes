package com.hospital.mes.integration.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class IntegrationApiContractTest {
    @Test
    void responseIsConcreteAndNeverExposesPayload() {
        assertThat(Arrays.stream(IntegrationMessageResponse.class.getRecordComponents()).map(RecordComponent::getName))
            .contains("messageRef","direction","messageId","status","retryCount","occurredAt","versionNo")
            .doesNotContain("payload","payloadJson");
    }
    @Test
    void messageReferenceIsStrictlyDirectionAndPositiveDatabaseId() {
        assertThat(IntegrationMessageRef.parse("INBOX:12").id()).isEqualTo(12);
        assertThatThrownBy(() -> IntegrationMessageRef.parse("INBOX:0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IntegrationMessageRef.parse("OTHER:1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void ifMatchAcceptsOnlyQuotedNonNegativeVersions() {
        assertThat(IntegrationMessageController.version("\"0\"")).isZero();
        assertThat(IntegrationMessageController.version("\"12\"")).isEqualTo(12);
        assertThatThrownBy(() -> IntegrationMessageController.version("12"))
            .isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
        assertThatThrownBy(() -> IntegrationMessageController.version("1\"2"))
            .isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
        assertThatThrownBy(() -> IntegrationMessageController.version("-1"))
            .isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
    }
}
