package com.hospital.mes.audit.attachment.application;

import com.hospital.mes.audit.application.CurrentPlatformContext;

/** A consumer may authorize only files actually associated with its regulated resource. */
public interface AttachmentAccessPolicy {
    boolean mayRead(CurrentPlatformContext context, long attachmentId);
}
