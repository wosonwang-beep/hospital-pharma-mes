import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.text.PDFTextStripper;
class ReceiptPdfReview {
 public static void main(String[] args) throws Exception {
  if(args.length!=2)throw new IllegalArgumentException("PDF input and output directory required");
  var out=Path.of(args[1]);Files.createDirectories(out);
  try(var pdf=Loader.loadPDF(Path.of(args[0]).toFile())){
   var renderer=new PDFRenderer(pdf);
   for(int page=0;page<pdf.getNumberOfPages();page++)ImageIO.write(renderer.renderImageWithDPI(page,150,ImageType.RGB),"PNG",out.resolve("page-"+(page+1)+".png").toFile());
   var text=new PDFTextStripper().getText(pdf);Files.writeString(out.resolve("text.txt"),text,StandardCharsets.UTF_8);
   System.out.println("PDFBOX pages="+pdf.getNumberOfPages()+" textCharacters="+text.length()+"; original PDF unchanged");
  }
 }
}
