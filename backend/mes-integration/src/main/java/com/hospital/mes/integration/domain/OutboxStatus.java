package com.hospital.mes.integration.domain;
public enum OutboxStatus { PENDING, DISPATCHING, RETRY_WAIT, PUBLISHED, DEAD_LETTER }
