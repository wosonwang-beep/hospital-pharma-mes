package com.hospital.mes.audit.signature;

public record ExpectedReauthenticationBinding(long userId, String sessionId, long organizationId,
                                               String objectType, String objectId,
                                               SignatureMeaning meaning, long recordVersion) { }
