/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneWord;

import luceneXML.*;
import java.io.InputStream;
import org.apache.lucene.document.Document;

/**
 *
 * @author deivakumaran dhanasegaran
 */
public interface DocumentHandler {
 /**
 * Creates a Lucene Document from an InputStream.
 * This method can return <code>null</code>.
 *
 * @param is the InputStream to convert to a Document
 * @return a ready-to-index instance of Document
 */
 Document getDocument(InputStream is);
}