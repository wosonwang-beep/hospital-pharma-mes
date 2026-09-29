package com.hospital.mes.security.api;

import com.hospital.mes.audit.signature.SignatureMeaning;

public final class ReauthenticateForSignatureRequest {
    private String objectType,objectId,credential; private SignatureMeaning meaning; private long recordVersion;
    public String getObjectType(){return objectType;}public void setObjectType(String v){objectType=v;}
    public String getObjectId(){return objectId;}public void setObjectId(String v){objectId=v;}
    public SignatureMeaning getMeaning(){return meaning;}public void setMeaning(SignatureMeaning v){meaning=v;}
    public long getRecordVersion(){return recordVersion;}public void setRecordVersion(long v){recordVersion=v;}
    public String getCredential(){return credential;}public void setCredential(String v){credential=v;}
    @Override public String toString(){return "ReauthenticateForSignatureRequest[objectType="+objectType+", objectId="+objectId+", credential=[REDACTED]]";}
}
