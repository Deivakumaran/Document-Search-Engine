/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneFinalProject;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author PeaceFull
 */
public class PdfBoxConverter {
     public String ToText(String filePath) throws IOException {

        PDFParser parser;
        PDFTextStripper pdfStripper;
        PDDocument pdDoc;
        COSDocument cosDoc;

        String Text;

        File file;

        file = new File(filePath);
        parser = new PDFParser(new RandomAccessFile(file, "r")); // update for PDFBox V 2.0

        parser.parse();
        cosDoc = parser.getDocument();
        pdfStripper = new PDFTextStripper();
        pdDoc = new PDDocument(cosDoc);
        pdDoc.getNumberOfPages();
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        Text = pdfStripper.getText(pdDoc);
        return Text;
    }

    public String pdfToTextConvert(File file) throws IOException {
        String contents = "";
        PDDocument doc = null;
        try {
            doc = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setLineSeparator("\n");
            stripper.setStartPage(1);
            stripper.setEndPage(doc.getNumberOfPages());// this mean that it will index the first 5 pages only
            contents = stripper.getText(doc);

        } catch (Exception e) {
            e.printStackTrace();
        }

        doc.close();
        return contents;
    }
}
