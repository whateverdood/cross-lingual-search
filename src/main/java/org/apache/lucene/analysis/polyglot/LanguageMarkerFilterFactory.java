package org.apache.lucene.analysis.polyglot;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 * Factory for {@link LanguageMarkerFilter}.
 * <code>
 * &lt;filter 
 *   class="org.apache.lucene.analysis.polyglot.LanguageMarkerFilterFactory"/&gt;
 * </code> */
public class LanguageMarkerFilterFactory extends TokenFilterFactory {

  protected LanguageMarkerFilterFactory(Map<String, String> args) {
    super(args);
  }

  @Override
  public TokenStream create(TokenStream input) {
    return new LanguageMarkerFilter(input);
  }

}
