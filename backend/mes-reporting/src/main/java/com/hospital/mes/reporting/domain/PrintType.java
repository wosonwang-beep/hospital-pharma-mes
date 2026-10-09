package com.hospital.mes.reporting.domain;
public enum PrintType {
 DOCUMENT, LIST;
 public static PrintType parse(String value){if(value==null||value.isBlank())return DOCUMENT;try{return valueOf(value);}catch(IllegalArgumentException ex){throw new IllegalArgumentException("打印类型只能为 DOCUMENT 或 LIST");}}
}
