package org.apache.lucene.analysis.polyglot;

import org.tartarus.snowball.SnowballProgram;

/**
 * Implementation that wraps a provided
 * {@link org.tartarus.snowball.SnowballProgram}.
 */
public class SnowballStemmer implements Stemmer {
  
  private SnowballProgram snowball;
  
  public SnowballStemmer(SnowballProgram snowball) {
    this.snowball = snowball;
  }

  @Override
  public String stem(String term) {
    if (null != snowball) {
      snowball.setCurrent(term);
      snowball.stem();
      return snowball.getCurrent();
    }
    return term;
  }
  
  @Override
  public String toString() {
    if (null != snowball) {      
      return snowball.toString();
    } else {
      return "I have no snowball :(";
    }
  }

}
