package com.hospital.mes.audit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.mes.common.exception.ValidationException;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.audit.signature.SignatureMeaning;
import com.hospital.mes.audit.signature.SignatureMeaningNotAllowedException;
import com.hospital.mes.audit.signature.UnsupportedSignableObjectException;
import org.junit.jupiter.api.Test;

class SignatureApiContractTest {
    @Test
    void ifMatchAcceptsOnlyQuotedNonNegativeVersions() {
        assertThat(SignatureController.version("\"0\"")).isZero();
        assertThat(SignatureController.version("\"12\"")).isEqualTo(12);
        assertThatThrownBy(() -> SignatureController.version("12"))
            .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> SignatureController.version("1\"2"))
            .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> SignatureController.version("-1"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void unsupportedProviderContractsMapToUnprocessableEntity() {
        assertThat(new UnsupportedSignableObjectException("UNKNOWN"))
            .isInstanceOf(ComplianceException.class);
        assertThat(new SignatureMeaningNotAllowedException("TEST_RECORD", SignatureMeaning.RELEASE))
            .isInstanceOf(ComplianceException.class);
    }
}
