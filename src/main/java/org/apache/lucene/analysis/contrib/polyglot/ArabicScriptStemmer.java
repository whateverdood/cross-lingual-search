package org.apache.lucene.analysis.contrib.polyglot;

import org.apache.lucene.analysis.ar.ArabicStemmer;

public class ArabicScriptStemmer implements ScriptStemmer {

  ArabicStemmer stemmer = new ArabicStemmer();

  @Override
  public String stem(String term) {
    char[] termBuffer = term.toCharArray();
    int stemmedLength = stemmer.stem(termBuffer, term.length());
    return new String(termBuffer, 0, stemmedLength);
  }

}
