package com.hospital.mes.audit.api;

import com.hospital.mes.audit.signature.SignatureMeaning;

public final class SignRecordRequest {
    private SignatureMeaning meaning;private String reauthToken;
    public SignatureMeaning getMeaning(){return meaning;}public void setMeaning(SignatureMeaning v){meaning=v;}
    public String getReauthToken(){return reauthToken;}public void setReauthToken(String v){reauthToken=v;}
    @Override public String toString(){return "SignRecordRequest[meaning="+meaning+", reauthToken=[REDACTED]]";}
}
