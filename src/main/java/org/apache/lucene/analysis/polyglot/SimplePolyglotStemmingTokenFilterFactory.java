package org.apache.lucene.analysis.polyglot;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 * Factory for {@link SimplePolyglotStemmingTokenFilter}. Supports preserving the
 * original token:
 * <code>
 * &lt;filter 
 *   class="org.apache.lucene.analysis.polyglot.SimplePolyglotStemmingTokenFilterFactory"
 *   preserveOriginalToken="true"/&gt;
 * </code>
 */
public class SimplePolyglotStemmingTokenFilterFactory extends TokenFilterFactory {
  
  private static final String PRESERVE_ORIGINAL_TOKEN = "preserveOriginalToken";
  private boolean preserveOriginalToken = true;

  protected SimplePolyglotStemmingTokenFilterFactory(Map<String, String> args) {
    super(args);
    preserveOriginalToken = args.containsKey(PRESERVE_ORIGINAL_TOKEN) ?
      Boolean.parseBoolean(args.get(PRESERVE_ORIGINAL_TOKEN)) : true;
  }

  @Override
  public TokenStream create(TokenStream tokenStream) {
    return new SimplePolyglotStemmingTokenFilter(tokenStream, preserveOriginalToken);
  }

}
