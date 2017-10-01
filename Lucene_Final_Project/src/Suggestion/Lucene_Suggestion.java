/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Suggestion;

import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.JTextArea;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author deivakumaran dhanasegaran
 */
public class Lucene_Suggestion {

    private  String INDEX_DIRECTORY=""; 
    private static final String INDEX_FILE_SPELL = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Spell";

    private static final String INDEX_FIELD = "contents";

    public void suggest(String searchWord, JTextArea suggestTextArea, String fileSelect) {
        try {
     
       if(fileSelect=="TEXT"){
             INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\TEXT_output";

        }
            else if(fileSelect=="PDF"){
              INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\PDF_Output";

        }
          else if(fileSelect=="XML"){
            
              INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\XML_Output";
        }
            
              
          else if(fileSelect=="WORD"){
               INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\WORD_Output";
      
          }
          
              
          else if(INDEX_DIRECTORY=="JSON"){
               INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\File_Format_Final_Project\\JSON_Output";
      
          }
          
        
            
            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer());

            //  read index conf
            IndexWriterConfig conf = new IndexWriterConfig(wrapper);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            // read dictionary
            // Directory directory = FSDirectory.open(new File(INDEX_FILE));
            //RAMDirectory ramDir = new RAMDirectory(directory, IOContext.READ);
       //     String INDEX_DIRECTORY = "E:\\Algorithms and Data Structure\\Summer-2017\\Final_Project\\Output";
            Directory indexDirectory = FSDirectory.open(
                    Paths.get(INDEX_DIRECTORY));

            DirectoryReader indexReader = DirectoryReader.open(indexDirectory);

            Dictionary dic = new LuceneDictionary(indexReader, INDEX_FIELD);

            SpellChecker sc = new SpellChecker(FSDirectory
                    .open(Paths.get(INDEX_FILE_SPELL)));
            //sc.indexDictionary(new PlainTextDictionary(new File("myfile.txt")), conf, false);
            sc.indexDictionary(dic, conf, true);
            String[] strs = sc.suggestSimilar(searchWord,
                    5);

            suggestTextArea.setText("");
            for (int i = 0; i < strs.length; i++) {
                // System.out.println(strs[i]);
                suggestTextArea.append(strs[i] + '\n');
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
