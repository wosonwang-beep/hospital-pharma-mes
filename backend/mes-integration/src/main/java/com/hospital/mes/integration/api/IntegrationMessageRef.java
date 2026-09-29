package com.hospital.mes.integration.api;
import com.hospital.mes.integration.domain.IntegrationDirection;
public record IntegrationMessageRef(IntegrationDirection direction,long id){
 public static IntegrationMessageRef parse(String value){if(value==null||!value.matches("^(INBOX|OUTBOX):[1-9][0-9]*$"))throw new IllegalArgumentException("invalid messageRef");int at=value.indexOf(':');return new IntegrationMessageRef(IntegrationDirection.valueOf(value.substring(0,at)),Long.parseLong(value.substring(at+1)));}
 @Override public String toString(){return direction+":"+id;}
}
