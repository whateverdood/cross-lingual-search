package org.apache.lucene.sandbox.analysis.polyglot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * A TokenFilter that uses the analyzers-icu {@link ScriptAttribute} to choose
 * which Stemmer to apply to the current token. Supports preserving the original
 * token in-place.
 *
 * @author rich
 */
public class SimplePolyGlotStemmingTokenFilter extends TokenFilter {

  private boolean preserveOriginal = false;
  private AttributeSource.State state;
  private ScriptAttribute scriptAttr;
  private CharTermAttribute termAttr;
  private PositionIncrementAttribute positionAttr;
  private List<String> stems = new ArrayList<String>();

  private Map<String, Object> stemmers = new HashMap<String, Object>();

  /**
   * Constructs a SimplePolyGlotStemmingTokenFilter that does not preserve
   * the original token (if stemmed).
   * @param input The source TokenStream
   */
  public SimplePolyGlotStemmingTokenFilter(TokenStream input) {
    this(input, false);
  }

  /**
   * Constructs a SimplePolyGlotStemmingTokenFilter.
   * @param input The source TokenStream
   * @param preserveOriginal If true, the original (un-stemmed) token will 
   * remain in the TokenStream at the same position as the stemmed version.
   */
  public SimplePolyGlotStemmingTokenFilter(TokenStream input, boolean preserveOriginal) {
    super(input);
    this.preserveOriginal = preserveOriginal;
    this.scriptAttr = input.addAttribute(ScriptAttribute.class);
    this.termAttr = input.addAttribute(CharTermAttribute.class);
    this.positionAttr = input.addAttribute(PositionIncrementAttribute.class);
    stemmers.put("Latin", new LatinScriptStemmer());
    stemmers.put("Arabic", new ArabicScriptStemmer());
    stemmers.put("Devanagari", new HindiScriptStemmer());
  }

  /**
   * The ush.
   */
  @Override
  public boolean incrementToken() throws IOException {
    if (stems.size() > 0) {
      restoreState(state);
      String s = stems.remove(0);
      termAttr.resizeBuffer(s.length());
      termAttr.copyBuffer(s.toCharArray(), 0, s.length());
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
   * @see SimplePolyGlotStemmingTokenFilter#preserveOriginal
   * @return true if the current token was stemmed.
   */
  private boolean stemmedToken() {
    String script = scriptAttr.getName();
    String term = termAttr.toString();

    System.out.println(String.format("(%s) [%s]", script, term));

    ScriptStemmer scriptStemmer = (ScriptStemmer) stemmers.get(script);
    
    if (null != scriptStemmer) {
      String s = scriptStemmer.stem(term);
      if (s.equalsIgnoreCase(term) == false) {
        System.out.println(String.format("[%s] stemmed to [%s]", term, s));
        if (preserveOriginal) {          
          stems.add(s);
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

