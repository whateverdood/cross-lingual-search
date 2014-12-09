package org.apache.lucene.analysis.polyglot;

/**
 * Common API for wrapping various Stemmer implementations.
 */
public interface Stemmer {
  public String stem(String term);
}
