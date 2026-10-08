package com.hospital.mes.reporting.application;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.*;
public final class EditorJwt {
 private final byte[] secret;private final Clock clock;private final ObjectMapper json=new ObjectMapper();
 public EditorJwt(String secret,Clock clock){if(secret==null||secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalArgumentException("Editor JWT secret must be supplied by deployment administrator (32 bytes minimum)");this.secret=secret.getBytes(StandardCharsets.UTF_8);this.clock=clock;}
 private byte[] mac(String value)throws Exception {Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));}
 private static String b64(byte[] bytes){return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 public String sign(Map<String,Object> payload){try{String body=b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.US_ASCII))+"."+b64(json.writeValueAsBytes(payload));return body+"."+b64(mac(body));}catch(Exception e){throw new IllegalStateException(e);}}
 public JsonNode verify(String token,String audience){try{if(token==null||token.length()>32768)throw new IllegalArgumentException();String[] p=token.split("\\.");if(p.length!=3||!MessageDigest.isEqual(mac(p[0]+"."+p[1]),Base64.getUrlDecoder().decode(p[2])))throw new IllegalArgumentException();var header=json.readTree(Base64.getUrlDecoder().decode(p[0]));if(!"HS256".equals(header.path("alg").asText()))throw new IllegalArgumentException();var body=json.readTree(Base64.getUrlDecoder().decode(p[1]));if(!body.isObject())throw new IllegalArgumentException();if(body.has("exp")&&(!body.path("exp").canConvertToLong()||body.path("exp").asLong()<=clock.instant().getEpochSecond()))throw new IllegalArgumentException();if(audience!=null&&(!audience.equals(body.path("aud").asText())||!body.path("exp").canConvertToLong()||body.path("exp").asLong()<=clock.instant().getEpochSecond()))throw new IllegalArgumentException();return body;}catch(Exception e){throw new com.hospital.mes.common.exception.PermissionException("EDITOR_TOKEN_INVALID","Invalid or expired editor authorization");}}
}
