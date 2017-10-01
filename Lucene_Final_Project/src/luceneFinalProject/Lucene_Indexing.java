/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneFinalProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.JTextArea;
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
 * @author PeaceFull
 */
public class Lucene_Indexing {

    StandardAnalyzer analyzer;
    public IndexSearcher searcher = null;

    String searchString;
    
JTextArea resultTextArea;
    public Lucene_Indexing(String searchString,JTextArea resultTextArea) {

          this.searchString = searchString;
          this.resultTextArea=resultTextArea;
                  
    }

   

    public void indexingPdfText() throws IOException, ParseException, InvalidTokenOffsetsException {

        PdfBoxConverter pdfConvert = new PdfBoxConverter();
        analyzer = new StandardAnalyzer();
        String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Output";
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Input_pdf";
        File dir = new File(input_Files_Path);
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {

                String text = pdfConvert.pdfToTextConvert(file);

                addPdfDocuments(writer, text, file.getPath(), file.getName());

            }
        }

        writer.close();     
        Lucene_Highlighter_Searching highlightSearch = new Lucene_Highlighter_Searching(indexDirectory, analyzer,searchString,resultTextArea);
        highlightSearch.searchMain(searchString);
       

    }

    public void indexingText() throws IOException, ParseException, InvalidTokenOffsetsException {

        PdfBoxConverter pdfConvert = new PdfBoxConverter();
        analyzer = new StandardAnalyzer();
        String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\text_output";
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Input";
        File dir = new File(input_Files_Path);
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                BufferedReader inputStream = new BufferedReader(new FileReader(file));
                addPlainTextDocuments(writer, inputStream, file.getPath(), file.getName());
            }
        }

        writer.close();
        Lucene_Highlighter_Searching highlightSearch = new Lucene_Highlighter_Searching(indexDirectory, analyzer,searchString,resultTextArea);
        highlightSearch.searchMain(searchString);

    }

    private static void addPdfDocuments(IndexWriter writer, String br, String filePath, String fileName) throws IOException {

        Document doc = new Document();
        doc.add(new TextField("contents", br, Field.Store.YES));
        doc.add(new StringField("fileName", fileName, Field.Store.YES));// use a string field for course_code because we don't want it tokenized
        doc.add(new StringField("filePath", filePath, Field.Store.YES));
        writer.addDocument(doc);

    }

    private static void addPlainTextDocuments(IndexWriter writer, BufferedReader br, String filePath, String fileName) throws IOException {

        Document doc = new Document();
        String line;
        String contents = "";
        while ((line = br.readLine()) != null) {
            contents = contents + line;
        }

        doc.add(new TextField("title", br));
        doc.add(new TextField("contents", contents, Field.Store.YES));
        doc.add(new StringField("fileName", fileName, Field.Store.YES));// use a string field for course_code because we don't want it tokenized
        doc.add(new StringField("filePath", filePath, Field.Store.YES));
        writer.addDocument(doc);
    }

    
}
