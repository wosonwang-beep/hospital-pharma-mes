package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import java.time.*;
import com.hospital.mes.common.exception.ResourceConflictException;
@Component @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingStore {
 private final IncomingMappers mappers;private final ObjectMapper json;
 public IncomingStore(IncomingMappers mappers,ObjectMapper json){this.mappers=mappers;this.json=json;}
 public IncomingRow create(String table){return mappers.create(table);}
 public IncomingRow get(String table,long org,long id){return one(table,new QueryWrapper<IncomingRow>().eq("org_id",org).eq("id",id));}
 public IncomingRow lock(String table,long org,long id){return one(table,new QueryWrapper<IncomingRow>().eq("org_id",org).eq("id",id).last("FOR UPDATE"));}
 private IncomingRow one(String table,QueryWrapper<IncomingRow> q){var row=mappers.mapper(table).selectOne(q);if(row==null)throw new NoSuchElementException("Resource not found");return row;}
 public List<IncomingRow> rows(String table,long org,String field,Object value){return mappers.mapper(table).selectList(new QueryWrapper<IncomingRow>().eq("org_id",org).eq(column(field),value).orderByAsc("id"));}
 public List<IncomingRow> all(String table,long org){return mappers.mapper(table).selectList(new QueryWrapper<IncomingRow>().eq("org_id",org).orderByDesc("id"));}
 public List<IncomingRow> lockedRows(String table,long org,String field,Object value){return mappers.mapper(table).selectList(new QueryWrapper<IncomingRow>().eq("org_id",org).eq(column(field),value).orderByAsc("id").last("FOR UPDATE"));}
 public void insert(String table,IncomingRow row,long org,long actor){var now=now();row.setOrgId(org);row.setCreatedBy(actor);row.setCreatedAt(now);if(row instanceof MutableIncomingRow m){m.setUpdatedBy(actor);m.setUpdatedAt(now);m.setVersionNo(0L);}mappers.mapper(table).insert(row);}
 public void update(String table,IncomingRow row,long expected,long actor,String...fields){if(!(row instanceof MutableIncomingRow m))throw new IllegalArgumentException("Immutable row");var u=new UpdateWrapper<IncomingRow>().eq("org_id",row.getOrgId()).eq("id",row.getId()).eq("version_no",expected);for(String f:fields)u.set(column(f),value(row,f));m.setUpdatedBy(actor);m.setUpdatedAt(now());m.setVersionNo(expected+1);u.set("updated_by",actor).set("updated_at",m.getUpdatedAt()).set("version_no",expected+1);if(mappers.mapper(table).update(null,u)!=1)throw new ResourceConflictException("VERSION_CONFLICT","Record changed");}
 public static Object value(IncomingRow row,String f){return new BeanWrapperImpl(row).getPropertyValue(f);}
 public static String text(IncomingRow row,String f){Object v=value(row,f);return v==null?null:v.toString();}
 public static Long number(IncomingRow row,String f){return (Long)value(row,f);}
 public static long version(IncomingRow row){return ((MutableIncomingRow)row).getVersionNo();}
 public static void set(IncomingRow row,String f,Object v){new BeanWrapperImpl(row).setPropertyValue(f,v);}
 public static String column(String f){if(!f.matches("[a-zA-Z][a-zA-Z0-9_]*"))throw new IllegalArgumentException("Invalid field");return f.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);}
 public static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
 public ObjectNode view(IncomingRow row){ObjectNode n=json.valueToTree(row);var names=new ArrayList<String>();n.fieldNames().forEachRemaining(names::add);for(String f:names){JsonNode v=n.get(f);if(v.isNull())continue;Object raw=value(row,f);if(f.equals("id")||f.equals("orgId")||f.endsWith("Id")||f.endsWith("By"))n.put(f,v.asText());else if(raw instanceof java.math.BigDecimal d)n.put(f,d.toPlainString());else if(raw instanceof LocalDateTime t)n.put(f,t.toInstant(ZoneOffset.UTC).toString());else if(f.endsWith("Json")){n.remove(f);try{n.set(f.substring(0,f.length()-4),json.readTree(v.asText()));}catch(Exception e){throw new IllegalStateException("Corrupt evidence JSON",e);}}}return n;}
}
