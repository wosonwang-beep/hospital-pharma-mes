package com.hospital.mes.common.web;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component public class TraceIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Trace-Id";
    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String supplied = request.getHeader(HEADER);
        String traceId = supplied != null && SAFE.matcher(supplied).matches() ? supplied : UUID.randomUUID().toString();
        MDC.put("traceId", traceId); response.setHeader(HEADER, traceId);
        try { chain.doFilter(request, response); } finally { MDC.remove("traceId"); }
    }
}
