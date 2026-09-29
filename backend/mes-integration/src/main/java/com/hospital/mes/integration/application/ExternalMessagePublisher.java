package com.hospital.mes.integration.application;
import com.hospital.mes.integration.domain.OutboxMessage;
public interface ExternalMessagePublisher { void publish(OutboxMessage message); }
