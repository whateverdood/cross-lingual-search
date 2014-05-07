package org.apache.lucene.sandbox.analysis.polyglot;

import org.apache.lucene.analysis.hi.HindiStemmer;

public class HindiScriptStemmer implements ScriptStemmer {

  HindiStemmer stemmer = new HindiStemmer();
  
  @Override
  public String stem(String term) {
    char[] termBuffer = term.toCharArray();
    int stemmedLength = stemmer.stem(termBuffer, term.length());
    return new String(termBuffer, 0, stemmedLength);
  }

}
