package com.hospital.mes.reporting.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
public interface EditorActorPort { CurrentPlatformContext requireActiveEditor(long organizationId,long actorId,String loginSessionId); }
