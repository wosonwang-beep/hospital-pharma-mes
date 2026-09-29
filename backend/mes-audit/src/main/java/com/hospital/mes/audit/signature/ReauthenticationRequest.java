package com.hospital.mes.audit.signature;

public final class ReauthenticationRequest {
    private final String objectType; private final String objectId; private final SignatureMeaning meaning;
    private final long recordVersion; private final String credential;
    public ReauthenticationRequest(String objectType, String objectId, SignatureMeaning meaning,
                                   long recordVersion, String credential) {
        this.objectType = objectType; this.objectId = objectId; this.meaning = meaning;
        this.recordVersion = recordVersion; this.credential = credential;
    }
    public String objectType(){return objectType;} public String objectId(){return objectId;}
    public SignatureMeaning meaning(){return meaning;} public long recordVersion(){return recordVersion;}
    public String credential(){return credential;}
    @Override public String toString(){return "ReauthenticationRequest[objectType="+objectType+", objectId="+objectId+
        ", meaning="+meaning+", recordVersion="+recordVersion+", credential=[REDACTED]]";}
}
