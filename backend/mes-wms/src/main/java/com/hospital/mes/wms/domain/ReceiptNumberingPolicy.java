package com.hospital.mes.wms.domain;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public final class ReceiptNumberingPolicy {
 private ReceiptNumberingPolicy() {}
 public static boolean monthly(String prefix,String pattern,int width){return "".equals(prefix)&&"yyyyMM".equals(pattern)&&width==4;}
 public static String period(ZoneId zone,Instant now){return DateTimeFormatter.ofPattern("uuuuMM",Locale.ROOT).withZone(zone).format(now);}
 public static String format(String prefix,String pattern,int width,long sequence,ZoneId zone,Instant now){
  boolean monthly=monthly(prefix,pattern,width);
  boolean legacy=prefix!=null&&prefix.matches("[A-Za-z0-9-]{1,20}")&&java.util.Set.of("yyyyMMdd","yyMMdd").contains(pattern)&&width>=1&&width<=18;
  if((!monthly&&!legacy)||sequence<1||zone==null||now==null)throw new IllegalArgumentException("Invalid approved numbering configuration");
  String digits=Long.toString(sequence);if(digits.length()>width)throw new IllegalStateException("Approved number sequence exhausted; review numbering rule");
  String padded="0".repeat(width-digits.length())+digits;
  return monthly?period(zone,now)+padded:prefix+"-"+DateTimeFormatter.ofPattern(pattern,Locale.ROOT).withZone(zone).format(now)+"-"+padded;
 }
}
