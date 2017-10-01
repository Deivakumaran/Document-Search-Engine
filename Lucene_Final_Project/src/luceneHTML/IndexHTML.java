/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneHTML;

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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

/**
 *
 * @author deivakumaran dhanasegaran
 */
public class IndexHTML implements DocumentHandler {

    String input_Files_Path = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\HTML_Input";
    String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\HTML_Output";
   
    String filePath = "";
    String fileName = "";

    String searchString = "";
    JTextArea resultTextArea;

    public IndexHTML(String searchString, JTextArea resultTextArea) {
        this.searchString = searchString;
        this.resultTextArea = resultTextArea;
    }

    public void initializePath(String filePath, String fileName) {

        this.filePath = filePath;
        this.fileName = fileName;
    }

    public org.apache.lucene.document.Document getDocument(InputStream is) {

        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = tidy.parseDOM(is, null);
        Element rawDoc = root.getDocumentElement();
        org.apache.lucene.document.Document doc
                = new org.apache.lucene.document.Document();
        String title = getTitle(rawDoc);
        String body = getBody(rawDoc);
        String contents=getContent(rawDoc);

        if ((title != null) && (!title.equals(""))) {
            doc.add(new TextField("title", title, Field.Store.YES));
            doc.add(new TextField("filePath", filePath, Field.Store.YES));
            doc.add(new TextField("fileName", fileName, Field.Store.YES));

        }
        if ((body != null) && (!body.equals(""))) {
            doc.add(new TextField("body", body, Field.Store.YES));
            doc.add(new TextField("filePath", filePath, Field.Store.YES));
            doc.add(new TextField("fileName", fileName, Field.Store.YES));

        }
        
        
        if ((contents != null) && (!contents.equals(""))) {
            
            doc.add(new TextField("contents", contents, Field.Store.YES));
            doc.add(new TextField("filePath", filePath, Field.Store.YES));
            doc.add(new TextField("fileName", fileName, Field.Store.YES));
        }
        return doc;
    }

    /**
     * Gets the title text of the HTML document.
     *
     * @rawDoc the DOM Element to extract title Node from
     * @return the title text
     */
    protected String getTitle(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }
        String title = "";
        NodeList children = rawDoc.getElementsByTagName("title");
        if (children.getLength() > 0) {
            Element titleElement = ((Element) children.item(0));
            Text text = (Text) titleElement.getFirstChild();
            if (text != null) {
                title = text.getData();
            }
        }
        return title;
    }

    /**
     * Gets the body text of the HTML document.
     *
     * @rawDoc the DOM Element to extract body Node from
     * @return the body text
     */
    protected String getBody(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }
        String body = "";
        NodeList children = rawDoc.getElementsByTagName("body");
        if (children.getLength() > 0) {
            body = getText(children.item(0));
        }
        return body;
    }

    
     protected String getContent(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }
        
          String title = "";
        NodeList children = rawDoc.getElementsByTagName("title");
        if (children.getLength() > 0) {
            Element titleElement = ((Element) children.item(0));
            Text text = (Text) titleElement.getFirstChild();
            if (text != null) {
                title = text.getData();
            }
        }
      
        String body = "";
        NodeList children1 = rawDoc.getElementsByTagName("body");
        
        if (children1.getLength() > 0) {
            body = getText(children1.item(0));
        }
        return title+body;
    }
    /**
     * Extracts text from the DOM node.
     *
     * @param node a DOM node
     * @return the text value of the node
     */

    protected String getText(Node node) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    sb.append(getText(child));
                    sb.append(" ");
                    break;
                case Node.TEXT_NODE:
                    sb.append(((Text) child).getData());
                    break;
            }
        }
        return sb.toString();
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
