package org.apache.lucene.analysis.polyglot;

import org.apache.lucene.analysis.hi.HindiStemmer;

/**
 * Implementation for stemming Hindi tokens that wraps a
 * {@link org.apache.lucene.analysis.hi.HindiStemmer}.
 */
public class HindiScriptStemmer implements Stemmer {

  HindiStemmer stemmer = new HindiStemmer();
  
  @Override
  public String stem(String term) {
    char[] termBuffer = term.toCharArray();
    int stemmedLength = stemmer.stem(termBuffer, term.length());
    return new String(termBuffer, 0, stemmedLength);
  }

}
