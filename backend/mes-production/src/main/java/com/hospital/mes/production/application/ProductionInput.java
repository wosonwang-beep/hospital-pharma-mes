package com.hospital.mes.production.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.production.domain.ProductionRules;
import java.math.*;
import java.time.*;
import java.util.*;
public final class ProductionInput {
 private ProductionInput(){}
 public static void fields(JsonNode body,String... names){if(body==null||!body.isObject())throw new IllegalArgumentException("Object request required");var allowed=Set.of(names);body.fieldNames().forEachRemaining(k->{if(!allowed.contains(k))throw new IllegalArgumentException("Unknown field: "+k);});}
 public static String text(JsonNode body,String key,int max){var node=body.get(key);if(node==null||!node.isTextual())throw new IllegalArgumentException(key+": string required");return MasterRules.text(node.asText(),max);}
 public static long id(JsonNode body,String key){return MasterMutation.id(text(body,key,30));}
 public static Long optionalId(JsonNode body,String key){return body.hasNonNull(key)?id(body,key):null;}
 public static long version(JsonNode body,String header){if(body.hasNonNull("versionNo")&&!body.get("versionNo").isIntegralNumber())throw new IllegalArgumentException("Integer version required");return MasterMutation.version(header,body.hasNonNull("versionNo")?body.get("versionNo").longValue():null);}
 public static BigDecimal quantity(JsonNode body,String key){String value=text(body,key,50);if(!value.matches("[0-9]+(\\.[0-9]{1,6})?"))throw new IllegalArgumentException(key+": decimal string required");var result=new BigDecimal(value);if(result.signum()<=0||result.precision()-result.scale()>12)throw new IllegalArgumentException(key+": positive DECIMAL(18,6) required");return result;}
 public static LocalDate date(JsonNode body,String key){return body.hasNonNull(key)?LocalDate.parse(text(body,key,10)):null;}
 public static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
 public static BigDecimal exact(MasterQueryService units,long org,long from,long to,Long material,BigDecimal value){var result=units.convert(org,from,to,material,value);var exact=value.multiply(new BigDecimal(result.factor()));ProductionRules.gate(exact.compareTo(new BigDecimal(result.convertedValue()))==0,"QUANTITY_PRECISION_LOSS","Conversion would lose precision");return exact;}
}
