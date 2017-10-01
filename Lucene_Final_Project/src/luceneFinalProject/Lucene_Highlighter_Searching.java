/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneFinalProject;

import Interface.MainJFrame;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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
 * @author PeaceFull
 */
public class Lucene_Highlighter_Searching {

    JTextArea resultTextArea;
    int count = 0;
    String searchString;
    Directory indexDirectory;
    StandardAnalyzer analyzer;
    int searchStatu = 0;
    int topHighlights = 5;
    int fragmetSize = 30;

    public Lucene_Highlighter_Searching(Directory indexDirectory, StandardAnalyzer analyzer, String searchString, JTextArea resultTextArea) {
        this.searchString = searchString;
        this.indexDirectory = indexDirectory;
        this.analyzer = analyzer;
        this.resultTextArea = resultTextArea;

    }

    public void searchMain(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        int wordLength = searchString.split("\\s+").length;
        if (wordLength == 1) {
            highlighterOneWordSearch(searchString);
        } else {
            highlighterPhraseSearch(searchString);
        }

    }

    public int highlighterPhraseSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~3";
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
                for (int i = 0; i < splitArray.length-1; i++) {
                    searchSt = splitArray[i] + "~";
                }

                highlighterOneWordSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                System.exit(0);
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");

            resultTextArea.append("Name of the file :" + " : " + d.get("fileName")+'\n');
            resultTextArea.append("Path of the file is :" + " : " + path+'\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append("======================="+'\n');
                System.out.println(fragment);
                resultTextArea.append(fragment);
            }

            //        System.out.println("Result"+(i + 1) + ". " + "\t" + d.get("fileName")+"\t"+d.get("filePath")+"\t"+printLines(d.get("filePath"),queryString));
        }

        return 1;
    }

    public int highlighterOneWordSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

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
        Formatter formatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmetSize);

        highlighter.setTextFragmenter(fragmenter);

        highlighter.setTextFragmenter(fragmenter);

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

                System.exit(0);
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");

            resultTextArea.append("Name of the file :" + " : " + d.get("fileName")+'\n');
            resultTextArea.append("Path of the file is :" + " : " + path+'\n');
            //Printing - to whappendich document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append("======================="+'\n');
                System.out.println(fragment);
                resultTextArea.append(fragment+'\n');
            }
            //        System.out.println("Result"+(i + 1) + ". " + "\t" + d.get("fileName")+"\t"+d.get("filePath")+"\t"+printLines(d.get("filePath"),queryString));
        }

        return 1;
    }

    public void fuzzyQuery(String searchString) throws IOException {

        QueryParser qp = new QueryParser("contents", analyzer);

        //qp.setDefaultOperator(QueryParser.Operator.OR);
        //Create the query
        // String a="dd"+;
        String query = searchString + "~";

        // 3. searching
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

        Query fuzzyQuery = new FuzzyQuery(
                new Term("contents", query), 2);

        ScoreDoc[] fuzzyHits = searcher.search(fuzzyQuery, hitsPerPage).scoreDocs;
        String[] fuzzyResults = new String[fuzzyHits.length];

        for (int i = 0; i < fuzzyHits.length; ++i) {
            int docId = fuzzyHits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println("Path " + " : " + d.get("filePath"));
            fuzzyResults[i] = d.get("label");
        }

        reader.close();
        //  return fuzzyResults;
    }

    
    
    
    public int HighlighterANDSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~3";
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
                for (int i = 0; i < splitArray.length-1; i++) {
                    searchSt = splitArray[i] + "~";
                }

                highlighterOneWordSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                System.exit(0);
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");

            resultTextArea.append("Name of the file :" + " : " + d.get("fileName")+'\n');
            resultTextArea.append("Path of the file is :" + " : " + path+'\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append("======================="+'\n');
                System.out.println(fragment);
                resultTextArea.append(fragment);
            }

            //        System.out.println("Result"+(i + 1) + ". " + "\t" + d.get("fileName")+"\t"+d.get("filePath")+"\t"+printLines(d.get("filePath"),queryString));
        }

        return 1;
    }
    
    
    
     public int HighlighterORSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~3";
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
                for (int i = 0; i < splitArray.length-1; i++) {
                    searchSt = splitArray[i] + "~";
                }

                highlighterOneWordSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                System.exit(0);
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");

            resultTextArea.append("Name of the file :" + " : " + d.get("fileName")+'\n');
            resultTextArea.append("Path of the file is :" + " : " + path+'\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append("======================="+'\n');
                System.out.println(fragment);
                resultTextArea.append(fragment);
            }

            //        System.out.println("Result"+(i + 1) + ". " + "\t" + d.get("fileName")+"\t"+d.get("filePath")+"\t"+printLines(d.get("filePath"),queryString));
        }

        return 1;
    }
    
     
     
     public int HighlighterNOTSearch(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        this.searchString = searchString;
        QueryParser qp = new QueryParser("contents", analyzer);

        String q = "\"" + searchString + "\"~3";
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
                for (int i = 0; i < splitArray.length-1; i++) {
                    searchSt = splitArray[i] + "~";
                }

                highlighterOneWordSearch(searchSt);
            } //processSearchString(searchString);
            else {
                System.out.println("No search document for keyword found");

                System.exit(0);
            }
        }

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;

            System.out.println("Score is :" + hits.scoreDocs[i].score);
            Document d = searcher.doc(docid);

            String path = d.get("filePath");

            resultTextArea.append("Name of the file :" + " : " + d.get("fileName")+'\n');
            resultTextArea.append("Path of the file is :" + " : " + path+'\n');
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + path);
            String text = d.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, topHighlights);
            for (String fragment : frags) {
                System.out.println("=======================");
                resultTextArea.append("======================="+'\n');
                System.out.println(fragment);
                resultTextArea.append(fragment);
            }

            //        System.out.println("Result"+(i + 1) + ". " + "\t" + d.get("fileName")+"\t"+d.get("filePath")+"\t"+printLines(d.get("filePath"),queryString));
        }

        return 1;
    }
    
    
    
    public void processSearchString(String searchString) throws IOException, ParseException, InvalidTokenOffsetsException {

        int status;
        String result = searchString;
        //     result = result.trim();

        result = result.trim().replaceAll(" +", " ");

        int wordLength = result.split("\\s+").length;

        //  System.out.println(wordLength);
        while (wordLength > 1) {

            result = result.substring(result.indexOf(' ') + 1);
            status = highlighterPhraseSearch(result);
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

            status = highlighterPhraseSearch(result);
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

        String[] splitArray = result.split("\\s+");
        for (int i = 0; i < splitArray.length; i++) {

            status = highlighterPhraseSearch(result);
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
