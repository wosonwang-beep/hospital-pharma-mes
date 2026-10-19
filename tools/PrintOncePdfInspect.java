import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import javax.imageio.ImageIO;

/** Artifact inspection only: existing local PDFBox, no network/database/application mutation. */
class PrintOncePdfInspect {
 public static void main(String[] args)throws Exception {
  Path file=Path.of(args[0]);Path out=Path.of(args[1]);String label=args[2];
  if(!label.matches("[a-z-]+"))throw new IllegalArgumentException("Label");
  Files.createDirectories(out);
  try(var doc=Loader.loadPDF(file.toFile())) {
   var renderer=new PDFRenderer(doc);var stripper=new PDFTextStripper();StringBuilder report=new StringBuilder();
   for(int i=0;i<doc.getNumberOfPages();i++) {
    var image=renderer.renderImageWithDPI(i,144);Path png=out.resolve(label+"-page-"+(i+1)+".png");ImageIO.write(image,"PNG",png.toFile());
    stripper.setStartPage(i+1);stripper.setEndPage(i+1);String text=stripper.getText(doc);Files.writeString(out.resolve(label+"-page-"+(i+1)+".txt"),text,StandardCharsets.UTF_8);
    report.append("page=").append(i+1).append(" size=").append(image.getWidth()).append("x").append(image.getHeight()).append(" textChars=").append(text.length()).append("\n");
   }
   String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
   String summary="file="+file+"\npages="+doc.getNumberOfPages()+"\nsha256="+digest+"\n"+report;
   Files.writeString(out.resolve(label+"-render-proof.txt"),summary,StandardCharsets.UTF_8);System.out.print(summary);
  }
 }
}
