package com.hospital.mes.audit.api;

import java.util.List;

public record AuditEventPageResponse(List<AuditEventResponse> items, int page, int size, long total) { }
