package com.hospital.mes.security.password;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemporaryPasswordGeneratorTest {
    @Test
    void generatesDistinctHighEntropyUrlSafeSecrets() {
        TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();
        String first = generator.generate();
        String second = generator.generate();
        assertThat(first).hasSize(24).matches("[A-Za-z0-9_-]{24}");
        assertThat(second).hasSize(24).matches("[A-Za-z0-9_-]{24}").isNotEqualTo(first);
    }
}
