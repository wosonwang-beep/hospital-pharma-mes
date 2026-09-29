package com.hospital.mes.common.api;

import java.util.List;

public record ApiError(String requestId, String code, String message,
                       List<FieldError> fieldErrors, List<String> allowedActions) {
    public ApiError {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
        allowedActions = allowedActions == null ? List.of() : List.copyOf(allowedActions);
    }

    public static ApiError of(String requestId, String code, String message) {
        return new ApiError(requestId, code, message, List.of(), List.of());
    }
}
