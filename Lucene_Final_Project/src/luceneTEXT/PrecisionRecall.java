/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneTEXT;

/**
 *
 * @author deivakumaran dhanasegaran
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.utils.*;
import org.apache.lucene.benchmark.quality.trec.*;
import org.apache.pdfbox.debugger.ui.textsearcher.Searcher;
 
// From appendix C
 
/* This code was extracted from the Lucene
   contrib/benchmark sources */
 
public class PrecisionRecall {
 
  public void main() throws Throwable {
 
 File topicsFile = new File("E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\topics.txt");
    File qrelsFile = new File("E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\qrels.txt");
   
      String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Output";
        Directory indexDirectory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
     //   IndexWriterConfig config = new IndexWriterConfig(analyzer);
      
    IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

    String docNameField = "filename";
 
    PrintWriter logger = new PrintWriter(System.out, true);
 
    TrecTopicsReader qReader = new TrecTopicsReader();   //#1
    QualityQuery qqs[] = qReader.readQueries(            //#1
        new BufferedReader(new FileReader(topicsFile))); //#1
 
    Judge judge = new TrecJudge(new BufferedReader(      //#2
        new FileReader(qrelsFile)));                     //#2
 
    judge.validateData(qqs, logger);                     //#3
 
    QualityQueryParser qqParser = new SimpleQQParser("title", "contents");  //#4
 
    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);
    SubmissionReport submitLog = null;
    QualityStats stats[] = qrun.execute(judge,           //#5
            submitLog, logger);
 
    QualityStats avg = QualityStats.average(stats);      //#6
    avg.log("SUMMARY",2,logger, "  ");
    indexDirectory.close();
  }
}
 