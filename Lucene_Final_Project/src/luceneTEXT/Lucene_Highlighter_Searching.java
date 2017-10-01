/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneTEXT;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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

/**
 *
 * @author deivakumaran dhanasegaran
 */
public class Lucene_Highlighter_Searching {

    JTextArea resultTextArea;
    int count = 0;
    String searchString;
    int queryResultCount = 0;
    String searchString2;
    Directory indexDirectory;
    StandardAnalyzer analyzer;
    int searchStatu = 0;
    int topHighlights = 5;
    int fragmetSize = 30;

    public Lucene_Highlighter_Searching(Directory indexDirectory, StandardAnalyzer analyzer, String searchString, String searchString2, JTextArea resultTextArea) {
        this.searchString = searchString;
        this.searchString2 = searchString2;
        this.indexDirectory = indexDirectory;
        this.analyzer = analyzer;
        this.resultTextArea = resultTextArea;

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
        } //if other operator is selected . Two text field is needed for search
        else {
            HighlighterOperatorSearch(searchString, searchString2, operatorSelect);
        }

    }

    public void highlighterPhraseSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~5";
        Query query = qp.parse(q);

        // 3. searching
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

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

                highlighterPhraseSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                resultTextArea.append("No search document for keyword found");
                return;
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;
            Document d = searcher.doc(docid);
            String path = d.get("filePath");
            System.out.println("Score is :" + hits.scoreDocs[i].score);
            resultTextArea.append((++queryResultCount)+"." +"Search Result of the query "+ '\n'+ '\n');
            resultTextArea.append("Score of the document :" + hits.scoreDocs[i].score+'\n');
            resultTextArea.append("Name of the searched file :" + d.get("fileName") + '\n');
            resultTextArea.append("Path of the searched file :" + path + '\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

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

    public void highlighterOneWordSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        //   String q = "\"" + searchString + "\"~5";
        Query query = qp.parse(searchString);

        // 3. searching
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs hits = searcher.search(query, hitsPerPage);

        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        if (hits.totalHits == 0) {
            //check fuzzyQuery
            count++;
            if (count == 1) {
                String searchSt = searchString + "~";

                highlighterOneWordSearch(searchSt);
            } // fuzzyQuery(searchString);
            //processSearchString(searchString);
            else {
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
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

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
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

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
          
            resultTextArea.append((++queryResultCount)+"." +"Search Result of the query "+ '\n'+ '\n');
            resultTextArea.append("Score of the document :" + hits.scoreDocs[i].score+'\n');
            resultTextArea.append("Name of the searched file :" + d.get("fileName") + '\n');
            resultTextArea.append("Path of the searched file :" + path + '\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

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

    public void processSearchString(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        int status = 0;
        String result = searchString;
        //     result = result.trim();

        result = result.trim().replaceAll(" +", " ");

        int wordLength = result.split("\\s+").length;

        //  System.out.println(wordLength);
        while (wordLength > 1) {

            result = result.substring(result.indexOf(' ') + 1);
            highlighterPhraseSearch(result);
            if (status == 1) {

                System.exit(0);
            }
            wordLength = result.split("\\s+").length;
        }
        //System.out.println(result);
        //System.out.println(wordLength);

        result = searchString;

        result = result.trim().replaceAll(" +", " ");

        wordLength = result.split("\\s+").length;
        while (wordLength > 1) {
            result = result.substring(0, result.lastIndexOf(' '));

            highlighterPhraseSearch(result);
            if (status == 1) {

                System.exit(0);
            }
            wordLength = result.split("\\s+").length;
        }
        result = searchString;

        result = result.trim().replaceAll(" +", " ");

        wordLength = result.split("\\s+").length;

        String[] splitArray = result.split("\\s+");
        for (int i = 0; i < splitArray.length; i++) {

            highlighterPhraseSearch(result);
            if (status == 1) {

                System.exit(0);
            }
            //  System.out.println(splitArray[i]);
        }

    }

    public int printLines(String filepath, String key) throws FileNotFoundException, IOException {
        int counter = 1;
        String line;

        // Read the file and display it line by line.
        BufferedReader file = new BufferedReader(new FileReader(filepath));
        while ((line = file.readLine()) != null) {
            if (line.contains(key)) {
                // System.out.println("\n"+counter + ": " + line);
                break;

            }
            counter++;
        }
        file.close();
        return counter;

    }
}
