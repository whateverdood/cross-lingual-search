package org.apache.lucene.analysis.polyglot;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.polyglot.tokenattributes.LanguageAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.RussianStemmer;

/**
 * A TokenFilter that uses LanguageAttributes to choose which Stemmer to apply 
 * to the current token. Supports preserving the original
 * token in-place.
 *
 * @author rich
 */
public class SimplePolyglotStemmingTokenFilter extends TokenFilter {

  private boolean preserveOriginal = true;
  private State state;
  private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
  private PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);
  private LanguageAttribute langAttr = addAttribute(LanguageAttribute.class);
  
  private Deque<String> tokenStack = new ArrayDeque<String>();

  private Map<String, Object> stemmers = new HashMap<String, Object>();

  /**
   * Constructs a SimplePolyglotStemmingTokenFilter that does not preserve
   * the original token (if stemmed).
   * @param input The source TokenStream
   */
  public SimplePolyglotStemmingTokenFilter(TokenStream input) {
    this(input, false);
  }

  /**
   * Constructs a SimplePolyglotStemmingTokenFilter.
   * @param input The source TokenStream
   * @param preserveOriginal If true, the original (un-stemmed) token will 
   * remain in the TokenStream at the same position as the stemmed version.
   */
  public SimplePolyglotStemmingTokenFilter(TokenStream input, boolean preserveOriginal) {
    super(input);
    this.preserveOriginal = preserveOriginal;
    
    // TODO: this must be configurable!
    stemmers.put("en", new SnowballStemmer(new EnglishStemmer()));
    stemmers.put("ar", new ArabicScriptStemmer());
    stemmers.put("ru", new SnowballStemmer(new RussianStemmer()));
    stemmers.put("hi", new HindiScriptStemmer());
    stemmers.put("fr", new SnowballStemmer(new FrenchStemmer()));
  }

  /**
   * The usual.
   */
  @Override
  public boolean incrementToken() throws IOException {
    if (tokenStack.size() > 0) {
      clearAttributes();
      restoreState(state);
      String stem = tokenStack.remove();
      termAttr.resizeBuffer(stem.length());
      termAttr.copyBuffer(stem.toCharArray(), 0, stem.length());
      positionAttr.setPositionIncrement(0);
      return true;
    }
    
    boolean incremented = input.incrementToken();

    if (incremented && stemmedToken() && preserveOriginal) {
      state = captureState();
    }

    return incremented;
  }

  /**
   * Try to stem the current token, storing the stem in a <code>List</code> if
   * preserveOriginal is <code>true</code>.
   * @see SimplePolyglotStemmingTokenFilter#preserveOriginal
   * @return true if the current token was stemmed.
   */
  private boolean stemmedToken() {
    String term = termAttr.toString();
    Stemmer stemmer = (Stemmer) stemmers.get(langAttr.getLang());
    
    if (null != stemmer) {
      String s = stemmer.stem(term);
      if (s.equalsIgnoreCase(term) == false) {
        if (preserveOriginal) {          
          tokenStack.add(s);
        } else {
          termAttr.resizeBuffer(s.length());
          termAttr.copyBuffer(s.toCharArray(), 0, s.length());
        }
        return true;
      }
    }
    return false;
  }

}

