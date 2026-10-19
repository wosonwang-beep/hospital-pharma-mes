package com.hospital.mes.ebr;

import com.fasterxml.jackson.databind.*;
import java.sql.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Explicit existing-schema guard; no fallback, provisioning, clearing or DEV connection. */
final class RoleIdIsolationSupport {
 static final String SCHEMA="test_mes_product_finished_20261011_0640";
 static final ObjectMapper JSON=new ObjectMapper();
 static String url(){String value=System.getenv("MES_ISOLATED_URL");assertThat(value).isEqualTo("jdbc:mariadb://127.0.0.1:3306/"+SCHEMA);return value;}
 static Connection connect() throws SQLException {var c=DriverManager.getConnection(url(),System.getenv("MES_ISOLATED_USER"),System.getenv("MES_ISOLATED_PASSWORD"));assertThat(scalar(c,"SELECT DATABASE()")).isEqualTo(SCHEMA);assertThat(scalar(c,"SELECT @@foreign_key_checks")).isEqualTo("1");try(var s=c.createStatement()){s.execute("SET SESSION time_zone='+00:00'");}return c;}
 static String scalar(Connection c,String sql)throws SQLException {try(var s=c.createStatement();var r=s.executeQuery(sql)){assertThat(r.next()).isTrue();return r.getString(1);}}
 static List<List<String>> rows(Connection c,String sql)throws SQLException {var out=new ArrayList<List<String>>();try(var s=c.createStatement();var r=s.executeQuery(sql)){while(r.next()){var row=new ArrayList<String>();for(int i=1;i<=r.getMetaData().getColumnCount();i++)row.add(r.getString(i));out.add(row);}}return out;}
 static Map<String,List<String>> columns(Connection c)throws SQLException {var out=new TreeMap<String,List<String>>();for(var r:rows(c,"SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME,ORDINAL_POSITION"))out.computeIfAbsent(r.get(0),t->new ArrayList<>()).add(r.get(1));return out;}
 static String id(String value){if(!value.matches("[A-Za-z0-9_]+"))throw new IllegalArgumentException("Unsafe identifier");return "`"+value+"`";}
 static List<List<String>> hexRows(Connection c,String table,List<String> columns)throws SQLException {String fields=columns.stream().map(x->"IF("+id(x)+" IS NULL,NULL,HEX(CAST("+id(x)+" AS BINARY)))").reduce((x,y)->x+","+y).orElseThrow();var rows=rows(c,"SELECT "+fields+" FROM "+id(table));rows.sort(Comparator.comparing(Object::toString));return rows;}
 static Map<String,List<List<String>>> snapshot(Connection c,Map<String,List<String>> columns)throws SQLException {var out=new TreeMap<String,List<List<String>>>();for(var e:columns.entrySet())out.put(e.getKey(),hexRows(c,e.getKey(),e.getValue()));return out;}
 static void verifyBackupAndSource(Connection c)throws Exception {
  Path folder=Path.of(Objects.requireNonNull(System.getenv("MES_ROLE_BACKUP_DIR"),"Verified isolated backup required"));var metadata=JSON.readTree(folder.resolve("new-backup.json").toFile());assertThat(metadata.path("schema").asText()).isEqualTo(SCHEMA);assertThat(metadata.path("dumpExitCode").asInt()).isZero();Path dump=Path.of(metadata.path("path").asText());assertThat(Files.size(dump)).isEqualTo(metadata.path("bytes").asLong());assertThat(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(dump)))).isEqualTo(metadata.path("sha256").asText());
  var integrity=JSON.readTree(folder.resolve("backup-integrity-verification.json").toFile());for(var check:integrity.path("content_checks"))assertThat(check.path("all_stored_bytes_equal").asBoolean()).isTrue();
  var source=JSON.readTree(folder.resolve("snapshot-hex-private.json").toFile());var actualColumns=columns(c);assertThat(actualColumns.keySet()).hasSize(138);
  var names=source.fieldNames();while(names.hasNext()){String table=names.next();var item=source.path(table);var cs=JSON.convertValue(item.path("columns"),new com.fasterxml.jackson.core.type.TypeReference<List<String>>(){});var expected=JSON.convertValue(item.path("rows"),new com.fasterxml.jackson.core.type.TypeReference<List<List<String>>>(){});expected.sort(Comparator.comparing(Object::toString));assertThat(hexRows(c,table,cs)).as("Backup source still exact: %s",table).isEqualTo(expected);}
 }
 static void report(String file,Object value)throws Exception {Path dir=Path.of(Objects.requireNonNull(System.getenv("MES_ROLE_EVIDENCE_DIR")));Files.createDirectories(dir);JSON.writerWithDefaultPrettyPrinter().writeValue(dir.resolve(file).toFile(),value);}
}
