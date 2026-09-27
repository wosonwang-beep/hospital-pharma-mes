package com.hospital.mes.common.trace;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
@Component public class MdcTraceIdProvider implements TraceIdProvider {
    public String currentTraceId() { return MDC.get("traceId"); }
}
