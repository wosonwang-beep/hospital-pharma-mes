package com.hospital.mes.process.domain;
/** Notifications for downstream modules; lifecycle gates never depend on a workflow engine. */
public final class ProcessEvents {
 private ProcessEvents(){}
 public record Submitted(long organizationId,long processVersionId){}
 public record Approved(long organizationId,long processVersionId){}
 public record Published(long organizationId,long processVersionId){}
}
