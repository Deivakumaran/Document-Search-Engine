/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneXML;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import luceneTEXT.Lucene_Highlighter_Searching;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author deivakumaran dhanasegaran
 */
public class IndexXML extends DefaultHandler implements DocumentHandler {
  
    String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\XML_Input";
    String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\XML_Output";
        
    private StringBuffer elementBuffer = new StringBuffer();
    private HashMap attributeMap;
    private Document doc;
    String entireFile = "";
    String searchString = "";
    String searchString2="";
    String filePath = "";
    String fileName = "";
    String operatorSelect="";
    JTextArea resultTextArea;

    public IndexXML(String searchString,String searchString2,String operatorSelect,JTextArea resultTextArea) {
        this.searchString = searchString;
          this.searchString2 = searchString2;
        this.resultTextArea = resultTextArea;
        this.operatorSelect=operatorSelect;
    }

    public void initializePath(String filePath, String fileName) {

        this.filePath = filePath;
        this.fileName = fileName;

    }

    @Override
    public Document getDocument(InputStream is) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser parser = spf.newSAXParser();
            parser.parse(is, this);
        } catch (ParserConfigurationException e) {
            System.out.println("error from SAXXMLHandler 2: " + e.getMessage());
        } catch (org.xml.sax.SAXException ex) {
            System.out.println("error from SAXXMLHandler 2: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(IndexXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        doc.add(new TextField("contents", entireFile, Field.Store.YES));
        doc.add(new TextField("filePath", filePath, Field.Store.YES));
        doc.add(new TextField("fileName", fileName, Field.Store.YES));

        return doc;
    }

    public void indeXML() throws IOException, ParseException, InvalidTokenOffsetsException {

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

        Lucene_Highlighter_Searching search = new Lucene_Highlighter_Searching(indexDirectory, analyzer, searchString,searchString2, resultTextArea);

        search.searchMain(searchString,searchString2,operatorSelect);
    }

    public void startDocument() {
        doc = new Document();
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        elementBuffer.setLength(0);
        if (atts.getLength() > 0) {
            attributeMap = new HashMap();
            for (int i = 0; i < atts.getLength(); i++) {
                attributeMap.put(atts.getQName(i), atts.getValue(i));
            }
        }
    }

    // called when cdata is found
    public void characters(char[] text, int start, int length) {
        elementBuffer.append(text, start, length);
    }

    // called at element end
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("address-book")) {
            return;
        } else if (qName.equals("contact")) {
            Iterator iter = attributeMap.keySet().iterator();
            while (iter.hasNext()) {
                String attName = (String) iter.next();
                String attValue = (String) attributeMap.get(attName);
                entireFile = entireFile + "\n" + attName + " : " + attValue;
            }
        } else {
            entireFile = entireFile + "\n" + qName + " : " + elementBuffer.toString();
        }

    }

}
