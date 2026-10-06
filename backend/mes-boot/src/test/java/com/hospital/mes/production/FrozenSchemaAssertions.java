package com.hospital.mes.production;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;

/** Independent response checks against the approved contract, including nested closed objects. */
public final class FrozenSchemaAssertions {
 private FrozenSchemaAssertions(){}
 static void assertProperty(ObjectMapper mapper,String name,String property,JsonNode actual){var api=load(mapper);check(api,api.path("components").path("schemas").path(name).path("properties").path(property),actual,"$"+name+"."+property);}
 public static void assertSchema(ObjectMapper mapper,String name,JsonNode actual){
  var api=load(mapper);check(api,api.path("components").path("schemas").path(name),actual,"$"+name);
 }
 private static JsonNode load(ObjectMapper mapper){
  try{Path root=Path.of("").toAbsolutePath();while(root!=null&&!Files.isRegularFile(root.resolve("MES_TASKS.md")))root=root.getParent();
   if(root==null)throw new AssertionError("Project root not found");
   var api=mapper.readTree(Files.readString(root.resolve("releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/08_OPENAPI_FULL_V1.0.21_FROZEN.yaml")));
   return api;
  }catch(java.io.IOException e){throw new AssertionError("Frozen schema unreadable",e);}
 }
 private static void check(JsonNode api,JsonNode spec,JsonNode actual,String path){
  if(spec.isMissingNode())throw new AssertionError("Missing schema "+path);
  if(actual!=null&&actual.isNull()&&spec.path("nullable").asBoolean())return;
  if(spec.has("$ref")){String ref=spec.path("$ref").asText();check(api,api.at(ref.substring(1)),actual,path);return;}
  if(spec.has("allOf")){for(var branch:spec.path("allOf"))check(api,branch,actual,path);return;}
  if(spec.has("oneOf")||spec.has("anyOf")){int matches=0;for(var branch:spec.path(spec.has("oneOf")?"oneOf":"anyOf"))try{check(api,branch,actual,path);matches++;}catch(AssertionError ignored){}
   require(spec.has("oneOf")?matches==1:matches>0,path+" union does not match");return;}
  if(actual==null||actual.isMissingNode())throw new AssertionError(path+" missing");
  Set<String> types=new HashSet<>();var declared=spec.path("type");if(declared.isArray())declared.forEach(t->types.add(t.asText()));else if(declared.isTextual())types.add(declared.asText());
  if(actual.isNull()){require(spec.path("nullable").asBoolean()||types.contains("null"),path+" null not allowed");return;}
  String type=declared.asText();
  if(declared.isArray()){type=actual.isObject()?"object":actual.isArray()?"array":actual.isTextual()?"string":actual.isBoolean()?"boolean":actual.isIntegralNumber()&&types.contains("integer")?"integer":actual.isNumber()?"number":"unknown";require(types.contains(type),path+" type union mismatch");}
  switch(type){
   case "object" -> {require(actual.isObject(),path+" must be object");for(var key:spec.path("required"))require(actual.has(key.asText()),path+" missing "+key.asText());
    actual.fields().forEachRemaining(e->{var property=spec.path("properties").get(e.getKey());if(property!=null)check(api,property,e.getValue(),path+"."+e.getKey());else require(!spec.has("additionalProperties")||!spec.path("additionalProperties").isBoolean()||spec.path("additionalProperties").asBoolean(),path+" undeclared "+e.getKey());});}
   case "array" -> {require(actual.isArray(),path+" must be array");bounds(spec,actual.size(),"minItems","maxItems",path);if(spec.has("items"))for(int i=0;i<actual.size();i++)check(api,spec.path("items"),actual.get(i),path+"["+i+"]");}
   case "string" -> {require(actual.isTextual(),path+" must be string");bounds(spec,actual.asText().length(),"minLength","maxLength",path);if(spec.has("pattern"))require(actual.asText().matches(spec.path("pattern").asText()),path+" pattern mismatch");}
   case "integer" -> {require(actual.isIntegralNumber(),path+" must be integer");bounds(spec,actual.asDouble(),"minimum","maximum",path);}
   case "number" -> {require(actual.isNumber(),path+" must be number");bounds(spec,actual.asDouble(),"minimum","maximum",path);}
   case "boolean" -> require(actual.isBoolean(),path+" must be boolean");
   default -> { }
  }
  if(spec.has("enum")){boolean allowed=false;for(var v:spec.path("enum"))if(v.equals(actual))allowed=true;require(allowed,path+" enum mismatch");}
 }
 private static void bounds(JsonNode spec,double actual,String min,String max,String path){if(spec.has(min))require(actual>=spec.path(min).asDouble(),path+" below "+min);if(spec.has(max))require(actual<=spec.path(max).asDouble(),path+" exceeds "+max);}
 private static void require(boolean pass,String message){if(!pass)throw new AssertionError(message);}
}
