package com.hospital.mes.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiErrorTest {
    @Test
    void defensivelyCopiesErrorCollections() {
        var fields = new ArrayList<>(List.of(new FieldError("reason", "REQUIRED", "Required")));
        var actions = new ArrayList<>(List.of("RETRY"));

        var error = new ApiError("request-1", "INVALID_INPUT", "Invalid input", fields, actions);
        fields.clear();
        actions.clear();

        assertEquals(1, error.fieldErrors().size());
        assertEquals(List.of("RETRY"), error.allowedActions());
        assertThrows(UnsupportedOperationException.class,
            () -> error.allowedActions().add("IGNORE"));
    }
}
