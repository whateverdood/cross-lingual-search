package org.apache.lucene.analysis.polyglot;

import org.apache.lucene.analysis.ar.ArabicStemmer;

/**
 * Implementation for stemming Arabic tokens that wraps a
 * {@link org.apache.lucene.analysis.ar.ArabicStemmer}.
 */
public class ArabicScriptStemmer implements Stemmer {

  ArabicStemmer stemmer = new ArabicStemmer();

  @Override
  public String stem(String term) {
    char[] termBuffer = term.toCharArray();
    int stemmedLength = stemmer.stem(termBuffer, term.length());
    return new String(termBuffer, 0, stemmedLength);
  }

}
