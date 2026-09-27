package com.hospital.mes.common.api;
public record ApiResponse<T>(String code, String message, T data, String traceId) {
    public static <T> ApiResponse<T> success(T data, String traceId) { return new ApiResponse<>("OK", "Success", data, traceId); }
}
