package com.hospital.mes.wms.domain;

/** Same-transaction request for a QMS-owned draft; never receiving or QC acceptance. */
public record FinishedInboundInspectionDraftRequested(long organizationId, long actorId,
        long inboundRequestId, String inspectionRequestNo, String reason, String idempotencyKey) {}
