package com.hospital.mes.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.common.api.ApiError;
import com.hospital.mes.common.trace.TraceIdProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorWriter {
    private final ObjectMapper json;
    private final TraceIdProvider traces;

    public SecurityErrorWriter(ObjectMapper json, TraceIdProvider traces) {
        this.json = json;
        this.traces = traces;
    }

    public void unauthorized(HttpServletResponse response) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required");
    }

    public void forbidden(HttpServletResponse response) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "Permission denied");
    }

    private void write(HttpServletResponse response, HttpStatus status, String code,
                       String message) throws IOException {
        if (response.isCommitted()) return;
        String traceId = traces.currentTraceId();
        if (traceId == null || traceId.isBlank()) traceId = response.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            response.setHeader("X-Trace-Id", traceId);
        }
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), ApiError.of(traceId, code, message));
    }
}
