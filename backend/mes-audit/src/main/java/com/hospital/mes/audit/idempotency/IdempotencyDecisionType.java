package com.hospital.mes.audit.idempotency;

public enum IdempotencyDecisionType { OWNER, REPLAY, CONFLICT, IN_PROGRESS_CONFLICT }
