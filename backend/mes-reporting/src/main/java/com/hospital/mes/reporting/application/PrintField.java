package com.hospital.mes.reporting.application;
public record PrintField(String key,String label,String group,String description,String example,boolean repeated,String dataType,String unitField,String datasetKey) {
 public PrintField(String key,String label,String group,String description,String example,boolean repeated,String dataType,String unitField){this(key,label,group,description,example,repeated,dataType,unitField,repeated?"items":null);}
 public PrintField(String key,String label,String group,String description,String example,boolean repeated){this(key,label,group,description,example,repeated,"TEXT",null);}
}
