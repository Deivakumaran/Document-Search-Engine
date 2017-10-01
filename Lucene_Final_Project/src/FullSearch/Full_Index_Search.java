/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FullSearch;

import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author deivakumaran dhanasegaran
 */
public class Full_Index_Search {

    String TEXT_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\TEXT_Output";
    String WORD_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\WORD_Output";
    String PDF_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\PDF_Output";
    String JSON_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\JSON_Output";
    String XML_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\XML_Output";
    String HTML_INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\HTML_Output";

    int queryResultCount = 0;
    JTextArea resultTextArea;
    int count = 0;
    String searchString;

    String searchString2;
    Directory indexDirectory;
    StandardAnalyzer analyzer = new StandardAnalyzer();
    ;
    int searchStatu = 0;
    int topHighlights = 2;
    int fragmetSize = 30;

    public Full_Index_Search(String searchString, String searchString2, JTextArea resultTextArea) {
        this.searchString = searchString;
        this.searchString2 = searchString2;

        this.resultTextArea = resultTextArea;

    }

    public Full_Index_Search(String mainSearch, String string, JTextField mainResultTextArea) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void searchMain(String searchString, String earchString2, String operatorSelect) throws IOException, ParseException, InvalidTokenOffsetsException {

        //if none of the operator is selected . Only one text field is needed for search
        int wordLength1 = searchString.split("\\s+").length;
        int wordLength2 = searchString2.split("\\s+").length;

        if (operatorSelect == "NONE") {
            if (wordLength1 == 1) {
                highlighterOneWordSearch(searchString);
            } else {
                highlighterPhraseSearch(searchString);
            }
        } else {
            HighlighterOperatorSearch(searchString, searchString2, operatorSelect);
        }
    }

    public void highlighterOneWordSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        //   String q = "\"" + searchString + "\"~5";
        Query query = qp.parse(searchString);

        // 3. searching
        int hitsPerPage = 10;

        Directory TextDir = FSDirectory.open(Paths.get(TEXT_INDEX_DIRECTORY));
        Directory WordDir = FSDirectory.open(Paths.get(WORD_INDEX_DIRECTORY));
        Directory PDFDir = FSDirectory.open(Paths.get(PDF_INDEX_DIRECTORY));
        Directory JSONDir = FSDirectory.open(Paths.get(JSON_INDEX_DIRECTORY));
        Directory XMLDir = FSDirectory.open(Paths.get(XML_INDEX_DIRECTORY));
        Directory HTMLDir = FSDirectory.open(Paths.get(HTML_INDEX_DIRECTORY));

        IndexReader readerText = DirectoryReader.open(TextDir);
        IndexReader readerWord = DirectoryReader.open(WordDir);
        IndexReader readerPDF = DirectoryReader.open(PDFDir);
        IndexReader readerJSON = DirectoryReader.open(JSONDir);
        IndexReader readerXML = DirectoryReader.open(XMLDir);
        IndexReader readerHTML = DirectoryReader.open(HTMLDir);

        IndexReader all_reader = new MultiReader(readerText, readerWord, readerPDF, readerJSON, readerXML, readerHTML);

        IndexSearcher searcher = new IndexSearcher(all_reader);

        TopDocs hits = searcher.search(query, hitsPerPage);

        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        if (hits.totalHits == 0) {
            //check fuzzyQuery
            count++;
            if (count == 1) {
                String searchSt = searchString + "~";

                highlighterOneWordSearch(searchSt);
            } else {
                System.out.println("No search document for keyword found");

                resultTextArea.append("No search document for keyword found");
                return;
            }
        }
        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmetSize);

        highlighter.setTextFragmenter(fragmenter);

        highlighter.setTextFragmenter(fragmenter);

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");
            resultTextArea.append((++queryResultCount)+"." +"Search Result of the query "+ '\n'+ '\n');
            resultTextArea.append("Score of the document :" + hits.scoreDocs[i].score+'\n');
            resultTextArea.append("Name of the searched file :" + d.get("fileName") + '\n');
            resultTextArea.append("Path of the searched file :" + path + '\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(all_reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append('\n' + "_______________________________________________________________________________________________________________________________________________" + '\n' + '\n');
                System.out.println(fragment);
                resultTextArea.append(fragment + '\n');

            }
            resultTextArea.append('\n' + "===============================================================================================================================================" + '\n' + '\n');
        }

    }

    public void highlighterPhraseSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~5";
        Query query = qp.parse(q);

        // 3. searching
        int hitsPerPage = 10;

        Directory TextDir = FSDirectory.open(Paths.get(TEXT_INDEX_DIRECTORY));
        Directory WordDir = FSDirectory.open(Paths.get(WORD_INDEX_DIRECTORY));
        Directory PDFDir = FSDirectory.open(Paths.get(PDF_INDEX_DIRECTORY));
        Directory JSONDir = FSDirectory.open(Paths.get(JSON_INDEX_DIRECTORY));
        Directory XMLDir = FSDirectory.open(Paths.get(XML_INDEX_DIRECTORY));
        Directory HTMLDir = FSDirectory.open(Paths.get(HTML_INDEX_DIRECTORY));

        IndexReader readerText = DirectoryReader.open(TextDir);
        IndexReader readerWord = DirectoryReader.open(WordDir);
        IndexReader readerPDF = DirectoryReader.open(PDFDir);
        IndexReader readerJSON = DirectoryReader.open(JSONDir);
        IndexReader readerXML = DirectoryReader.open(XMLDir);
        IndexReader readerHTML = DirectoryReader.open(HTMLDir);

        IndexReader all_reader = new MultiReader(readerText, readerWord, readerPDF, readerJSON, readerXML, readerHTML);

        IndexSearcher searcher = new IndexSearcher(all_reader);

        TopDocs hits = searcher.search(query, hitsPerPage);

        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmetSize);

        highlighter.setTextFragmenter(fragmenter);

        highlighter.setTextFragmenter(fragmenter);

        if (hits.totalHits == 0) {
            count++;
            if (count == 1) {
                String[] splitArray = searchString.split("\\s+");
                String searchSt = "";
                for (int i = 0; i < splitArray.length; i++) {
                    searchSt = splitArray[i] + "~";
                }

                highlighterPhraseSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found" + '\n' + "Please try searching appropriate words or Use suggestion for words to be searched");

                resultTextArea.append("No search document for keyword found" + '\n' + "Please try searching appropriate words or Use suggestion for words to be searched");

                return;
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");
            resultTextArea.append((++queryResultCount) + "." + "Search Result of the query " + '\n' + '\n');
            resultTextArea.append("Score of the document :" + hits.scoreDocs[i].score + '\n');
            resultTextArea.append("Name of the searched file :" + d.get("fileName") + '\n');
            resultTextArea.append("Path of the searched file :" + path + '\n');

            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(all_reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append('\n' + "_______________________________________________________________________________________________________________________________________________" + '\n' + '\n');
                System.out.println(fragment);
                resultTextArea.append(fragment + '\n');

            }
            resultTextArea.append('\n' + "===============================================================================================================================================" + '\n' + '\n');
        }

    }

    public void HighlighterOperatorSearch(String searchString, String searchString2, String operatorSelect) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        this.searchString2 = searchString2;
        QueryParser qp = new QueryParser("contents", analyzer);

        String op = operatorSelect;
        String q = "\"" + searchString + "\"" + op + "\"" + searchString2 + "\"";

        Query query = qp.parse(q);

        // 3. searching
        int hitsPerPage = 10;
        Directory TextDir = FSDirectory.open(Paths.get(TEXT_INDEX_DIRECTORY));
        Directory WordDir = FSDirectory.open(Paths.get(WORD_INDEX_DIRECTORY));
        Directory PDFDir = FSDirectory.open(Paths.get(PDF_INDEX_DIRECTORY));
        Directory JSONDir = FSDirectory.open(Paths.get(JSON_INDEX_DIRECTORY));
        Directory XMLDir = FSDirectory.open(Paths.get(XML_INDEX_DIRECTORY));
        Directory HTMLDir = FSDirectory.open(Paths.get(HTML_INDEX_DIRECTORY));

        IndexReader readerText = DirectoryReader.open(TextDir);
        IndexReader readerWord = DirectoryReader.open(WordDir);
        IndexReader readerPDF = DirectoryReader.open(PDFDir);
        IndexReader readerJSON = DirectoryReader.open(JSONDir);
        IndexReader readerXML = DirectoryReader.open(XMLDir);
        IndexReader readerHTML = DirectoryReader.open(HTMLDir);

        IndexReader all_reader = new MultiReader(readerText, readerWord, readerPDF, readerJSON, readerXML, readerHTML);

        IndexSearcher searcher = new IndexSearcher(all_reader);

        TopDocs hits = searcher.search(query, hitsPerPage);

        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmetSize);

        highlighter.setTextFragmenter(fragmenter);

        highlighter.setTextFragmenter(fragmenter);

        if (hits.totalHits == 0) {
            count++;
            if (count == 1) {
                String[] splitArray = searchString.split("\\s+");
                String searchSt = "";
                for (int i = 0; i < splitArray.length - 1; i++) {
                    searchSt = splitArray[i] + "~";

                }
                System.out.println(searchSt);

                String[] splitArray2 = searchString2.split("\\s+");
                String searchSt2 = "";
                for (int i = 0; i < splitArray2.length - 1; i++) {
                    searchSt2 = splitArray2[i] + "~";
                }

                System.out.println(searchSt2);

                HighlighterOperatorSearch(searchSt, searchSt2, operatorSelect);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                resultTextArea.append("No search document for keyword found");
                return;
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");
            resultTextArea.append((++queryResultCount) + "." + "Search Result of the query " + '\n' + '\n');
            resultTextArea.append("Score of the document :" + hits.scoreDocs[i].score + '\n');
            resultTextArea.append("Name of the searched file :" + d.get("fileName") + '\n');
            resultTextArea.append("Path of the searched file :" + path + '\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(all_reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append('\n' + "_______________________________________________________________________________________________________________________________________________" + '\n' + '\n');
                System.out.println(fragment);
                resultTextArea.append(fragment + '\n');

            }
            resultTextArea.append('\n' + "===============================================================================================================================================" + '\n' + '\n');

        }

    }

}
