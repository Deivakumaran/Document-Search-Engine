/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenePDF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.JTextArea;
import luceneTEXT.Lucene_Highlighter_Searching;
import org.apache.log4j.BasicConfigurator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author deivakumaran dhanasegaran
 */

public class IndexPDF {

    String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\PDF_Input";
    String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\PDF_Output";
    

StandardAnalyzer analyzer;
    public IndexSearcher searcher = null;

    String searchString;
    
    String searchString2;
    
   JTextArea resultTextArea;
    public IndexPDF(String searchString,String searchString2,JTextArea resultTextArea) {

          this.searchString = searchString;
          this.searchString2=searchString2;
          this.resultTextArea=resultTextArea;
                  
    }

    public void indexingPdfText(String operatorSelect) throws IOException, ParseException, InvalidTokenOffsetsException {
        BasicConfigurator.configure();
        
         PDFConverter pdfConvert= new PDFConverter();
        analyzer = new StandardAnalyzer();
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        File dir = new File(input_Files_Path);
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {

                String text = pdfConvert.pdfToTextConvert(file);

                addPdfDocuments(writer, text, file.getPath(), file.getName());

            }
        }

        writer.close();     
        Lucene_Highlighter_Searching highlightSearch = new Lucene_Highlighter_Searching(indexDirectory, analyzer,searchString,searchString2,resultTextArea);
        highlightSearch.searchMain(searchString,searchString2,operatorSelect);
       

    }

    private static void addPdfDocuments(IndexWriter writer, String br, String filePath, String fileName) throws IOException {

        Document doc = new Document();
        doc.add(new TextField("contents", br, Field.Store.YES));
        doc.add(new StringField("fileName", fileName, Field.Store.YES));// use a string field for course_code because we don't want it tokenized
        doc.add(new StringField("filePath", filePath, Field.Store.YES));
        writer.addDocument(doc);

    }

   
    
}
