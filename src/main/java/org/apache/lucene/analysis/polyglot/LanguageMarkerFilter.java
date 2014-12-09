package org.apache.lucene.analysis.polyglot;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.polyglot.charfilter.LangIdCharFilter;
import org.apache.lucene.analysis.polyglot.tokenattributes.LanguageAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * Marks each token with its {@link LanguageAttribute}. Token languages
 * are identified upstream by a {@link LangIdCharFilter}. 
 */
public class LanguageMarkerFilter extends TokenFilter {
  
  private LanguageAttribute langAttr = addAttribute(LanguageAttribute.class);
  private OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
  
  private LangIdCharFilter.LanguageOffsets langOffsets = 
    LangIdCharFilter.getLanguageOffsets();

  protected LanguageMarkerFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      String lang = langOffsets.langAtOffset(offsetAttr.startOffset());
      langAttr.setLang(lang);
      return true;
    }
    return false;
  }

}
