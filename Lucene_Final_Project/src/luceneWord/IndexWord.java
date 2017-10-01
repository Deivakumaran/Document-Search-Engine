/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneWord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javax.swing.JTextArea;
import luceneTEXT.Lucene_Highlighter_Searching;
import luceneXML.DocumentHandler;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
/**
 *
 * @author deivakumaran dhanasegaran
 */
public class IndexWord implements DocumentHandler {

    String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\WORD_Input";
    String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\WORD_Output";
    String filePath = "";
    String fileName = "";

    String searchString = "";
    JTextArea resultTextArea;

    public IndexWord(String searchString, JTextArea resultTextArea) {
        this.searchString = searchString;
        this.resultTextArea = resultTextArea;
    }

    public void initializePath(String filePath, String fileName) {

        this.filePath = filePath;
        this.fileName = fileName;
    }

        @Override
        public Document getDocument(InputStream is) {
            String bodyText = null;
            try {
                XWPFDocument wd = new XWPFDocument(is);
                XWPFWordExtractor we = new XWPFWordExtractor(wd);
                bodyText = we.getText();
            } catch (Exception e) {
                System.out.println("Error from POIWordDocHandler 1" + e.getMessage());
            }

            if ((bodyText != null) && (bodyText.trim().length() > 0)) {
                Document doc = new Document();
                doc.add(new TextField("contents", bodyText, Field.Store.YES));
                doc.add(new TextField("filePath", filePath, Field.Store.YES));
                doc.add(new TextField("fileName", fileName, Field.Store.YES));

                return doc;
            }
            return null;
        }
    

    public void main() throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        final Path path = Paths.get(input_Files_Path);

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        InputStream stream = Files.newInputStream(file);
                        initializePath(file.toString(), file.getFileName().toString());
                        Document doc = getDocument(stream);
                        writer.addDocument(doc);
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            InputStream stream1 = Files.newInputStream(path);
            initializePath(path.toString(), path.getFileName().toString());

            Document doc = getDocument(stream1);
            writer.addDocument(doc);

        }

        writer.close();
        Lucene_Highlighter_Searching search = new Lucene_Highlighter_Searching(indexDirectory, analyzer, searchString, "", resultTextArea);
        search.highlighterPhraseSearch(searchString);

    }
}
