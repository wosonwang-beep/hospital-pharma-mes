package com.hospital.mes.audit.signature;

import com.hospital.mes.audit.application.CurrentPlatformContext;

public record SignatureValidationContext(CurrentPlatformContext platform, SignatureMeaning meaning) { }
