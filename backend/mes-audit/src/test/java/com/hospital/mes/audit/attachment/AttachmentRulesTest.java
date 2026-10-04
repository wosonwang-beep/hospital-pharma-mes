package com.hospital.mes.audit.attachment;

import com.hospital.mes.audit.attachment.domain.AttachmentRules;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AttachmentRulesTest {
    @Test void onlyNonemptyBoundedFilesAreAccepted() {
        assertThatThrownBy(() -> AttachmentRules.validateSize(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AttachmentRules.validateSize(10_485_761)).isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> AttachmentRules.validateSize(10_485_760)).doesNotThrowAnyException();
    }
    @Test void downloadNameCannotCarryPathsOrControlCharacters() {
        assertThat(AttachmentRules.fileName("C:\\temp\\coa\r\n.pdf")).isEqualTo("coa.pdf");
        assertThat(AttachmentRules.fileName("../../coa.pdf")).isEqualTo("coa.pdf");
        assertThatThrownBy(() -> AttachmentRules.fileName("x".repeat(256))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void unknownMediaTypeDoesNotBecomeAResponseHeader() {
        assertThat(AttachmentRules.mediaType("application/pdf")).isEqualTo("application/pdf");
        assertThat(AttachmentRules.mediaType("text/plain\r\nX-Evil: true")).isEqualTo("application/octet-stream");
    }
    @Test void contentDigestIsOfExactBytes() {
        assertThat(AttachmentRules.digest(new byte[]{97,98,99}))
            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(AttachmentRules.digest(new byte[]{97,98,99,10}))
            .isNotEqualTo(AttachmentRules.digest(new byte[]{97,98,99}));
    }
}
