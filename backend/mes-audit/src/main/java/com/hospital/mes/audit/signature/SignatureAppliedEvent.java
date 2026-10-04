package com.hospital.mes.audit.signature;

import com.hospital.mes.audit.application.CurrentPlatformContext;

/** Synchronous notification inside the signature transaction; listener failure rolls it back. */
public record SignatureAppliedEvent(CurrentPlatformContext context, SignatureRecord signature) {}
