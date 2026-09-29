package com.hospital.mes.audit.signature;

import com.hospital.mes.audit.application.CurrentPlatformContext;

public final class SignCommand {
    private final CurrentPlatformContext context; private final String objectType; private final String objectId;
    private final SignatureMeaning meaning; private final long recordVersion; private final String reauthToken;
    private final String idempotencyKey; private final Long revokedSignatureId;
    public SignCommand(CurrentPlatformContext context,String objectType,String objectId,SignatureMeaning meaning,
                       long recordVersion,String reauthToken,String idempotencyKey,Long revokedSignatureId){
        this.context=context;this.objectType=objectType;this.objectId=objectId;this.meaning=meaning;
        this.recordVersion=recordVersion;this.reauthToken=reauthToken;this.idempotencyKey=idempotencyKey;
        this.revokedSignatureId=revokedSignatureId;
    }
    public CurrentPlatformContext context(){return context;} public String objectType(){return objectType;}
    public String objectId(){return objectId;} public SignatureMeaning meaning(){return meaning;}
    public long recordVersion(){return recordVersion;} public String reauthToken(){return reauthToken;}
    public String idempotencyKey(){return idempotencyKey;} public Long revokedSignatureId(){return revokedSignatureId;}
    @Override public String toString(){return "SignCommand[objectType="+objectType+", objectId="+objectId+
        ", meaning="+meaning+", recordVersion="+recordVersion+", reauthToken=[REDACTED]]";}
}
