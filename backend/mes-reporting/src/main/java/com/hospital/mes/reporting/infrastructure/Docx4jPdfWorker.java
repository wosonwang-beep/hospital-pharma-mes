package com.hospital.mes.reporting.infrastructure;
import com.hospital.mes.reporting.application.DocxGuard;
import com.hospital.mes.common.exception.ComplianceException;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
/** Only conversion work, no Spring context, datasource or network service. Launched by the bounded parent. */
public final class Docx4jPdfWorker {
 private Docx4jPdfWorker(){}
 public static void main(String[] args){
  try{
   if(args.length!=3)throw new IllegalArgumentException("Expected job paths");Path root=Path.of(args[2]).toAbsolutePath().normalize();Path input=Path.of(args[0]).toAbsolutePath().normalize(),output=Path.of(args[1]).toAbsolutePath().normalize();
   if(!input.equals(root.resolve("input.docx"))||!output.equals(root.resolve("output.pdf"))||!Files.isRegularFile(input,LinkOption.NOFOLLOW_LINKS)||Files.size(input)>5*1024*1024)throw new IllegalArgumentException("Invalid job paths");
   byte[] docx=Files.readAllBytes(input);new DocxGuard().validateStructure(docx);Files.write(output,render(docx),StandardOpenOption.CREATE_NEW);
  }catch(Throwable failure){System.exit(2);}
 }
 private static byte[] render(byte[] docx)throws Exception{
  try(var out=new ByteArrayOutputStream()){
   var packageDoc=WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
   Mapper fonts=new IdentityPlusMapper();
   // Use installed, licensed CJK fonts; never embed or redistribute proprietary font files.
   PhysicalFont chinese=PhysicalFonts.get("Microsoft YaHei");
   if(chinese==null)chinese=PhysicalFonts.get("Noto Sans CJK SC");
   if(chinese!=null){
    fonts.put("Microsoft YaHei",chinese);
    fonts.put("SimSun",chinese);
   }
   packageDoc.setFontMapper(fonts);
   FOSettings settings=Docx4J.createFOSettings();
   settings.setWmlPackage(packageDoc);
   settings.setFopConfig(org.docx4j.fonts.fop.util.FopConfigUtil.createConfigurationObject(fonts,packageDoc.getMainDocumentPart().fontsInUse()));
   Docx4J.toFO(settings,out,Docx4J.FLAG_EXPORT_PREFER_XSL);
   byte[] bytes=out.toByteArray();
   if(bytes.length<100||bytes.length>25*1024*1024||!"%PDF-".equals(new String(bytes,0,5,StandardCharsets.US_ASCII)))
    throw new ComplianceException("PRINT_CONVERSION_FAILED","转换器未产出有效 PDF");
   return bytes;
  }
 }
}
