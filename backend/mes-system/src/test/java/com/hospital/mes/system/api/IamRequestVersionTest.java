package com.hospital.mes.system.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IamRequestVersionTest {
    @Test
    void acceptsQuotedStrongEntityTag() {
        assertEquals(42L, IamRequestVersion.parse("\"42\""));
    }

    @Test
    void rejectsWeakUnquotedAndNegativeEntityTags() {
        assertThrows(IllegalArgumentException.class, () -> IamRequestVersion.parse("W/\"1\""));
        assertThrows(IllegalArgumentException.class, () -> IamRequestVersion.parse("1"));
        assertThrows(IllegalArgumentException.class, () -> IamRequestVersion.parse("\"-1\""));
    }
}
