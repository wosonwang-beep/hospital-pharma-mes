package com.hospital.mes.common.web;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
class TraceIdFilterTest {
    private final TraceIdFilter filter = new TraceIdFilter();
    @Test void preservesSafeTraceIdAndClearsMdc() throws Exception {
        var req=new MockHttpServletRequest(); req.addHeader(TraceIdFilter.HEADER,"safe-123"); var res=new MockHttpServletResponse();
        filter.doFilter(req,res,(a,b)->assertEquals("safe-123",MDC.get("traceId")));
        assertEquals("safe-123",res.getHeader(TraceIdFilter.HEADER)); assertNull(MDC.get("traceId"));
    }
    @Test void replacesUnsafeTraceId() throws Exception {
        var req=new MockHttpServletRequest(); req.addHeader(TraceIdFilter.HEADER,"bad\nvalue"); var res=new MockHttpServletResponse();
        filter.doFilter(req,res,(a,b)->{}); assertNotEquals("bad\nvalue",res.getHeader(TraceIdFilter.HEADER));
    }
}
