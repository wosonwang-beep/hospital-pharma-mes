package com.hospital.mes.integration.application;
import com.hospital.mes.integration.domain.InboxMessage;
public interface InboxMessageHandler { void handle(InboxMessage message); }
