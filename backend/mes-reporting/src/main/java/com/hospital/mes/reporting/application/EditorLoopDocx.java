package com.hospital.mes.reporting.application;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;import java.util.*;
public final class EditorLoopDocx {
 private EditorLoopDocx(){}
 public static byte[] create(List<String> columns,List<PrintField> fields){var allowed=new HashMap<String,PrintField>();fields.stream().filter(PrintField::repeated).forEach(f->allowed.put(f.key(),f));if(columns.isEmpty()||columns.size()>8||new HashSet<>(columns).size()!=columns.size()||!allowed.keySet().containsAll(columns))throw new IllegalArgumentException("请选择1至8个不同的检验项目列");
  try(var d=new XWPFDocument();var out=new ByteArrayOutputStream()){var table=d.createTable(2,columns.size());table.setWidth("100%");table.getRow(0).setRepeatHeader(true);for(int i=0;i<columns.size();i++){var f=allowed.get(columns.get(i));table.getRow(0).getCell(i).setText(f.label());table.getRow(0).getCell(i).setColor("EAF0F8");var p=table.getRow(1).getCell(i).getParagraphs().getFirst();if(i==0)control(p,"mes-loop:items","检验项目循环");control(p,"mes-item:"+f.key(),f.label());}for(var row:table.getRows())row.setCantSplitRow(true);d.write(out);return out.toByteArray();}catch(IOException e){throw new IllegalStateException(e);}}
 public static void control(XWPFParagraph p,String tag,String label){var sdt=p.getCTP().addNewSdt();var props=sdt.addNewSdtPr();props.addNewTag().setVal(tag);props.addNewAlias().setVal(label);var run=sdt.addNewSdtContent().addNewR();var fonts=run.addNewRPr().addNewRFonts();fonts.setAscii("Microsoft YaHei");fonts.setEastAsia("Microsoft YaHei");run.addNewT().setStringValue("【"+label+"】");}
}
