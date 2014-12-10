package org.apache.lucene.analysis.polyglot.charfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
import org.apache.lucene.analysis.polyglot.TokenToolbox;
import org.apache.lucene.analysis.polyglot.charfilter.LangIdCharFilter.LanguageOffsets;
import org.junit.Test;

public class LangIdCharFilterTest {
  
  private static final Map<String, String> NO_ARGS = new HashMap<String, String>();

  private void assertLangInOffsetRangeIs(
    LanguageOffsets langOffsets, String lang, int start, int end) {
    for (int i = start; i < end; i++) {
      assertTextAtOffsetIs(langOffsets, lang, i);
    }
  }

  private void assertTextAtOffsetIs(LanguageOffsets langOffs, String lang, int i) {
    assertEquals(String.format("Wrong lang [%s] at offset [%d]", lang, i), 
      lang, langOffs.langAtOffset(i));
  }
  
  TokenToolbox tokenToollbox = new TokenToolbox();

  String mixed = "Je peux manger tout cela ici, j'ai un faim de loup! "
    + "I am so hungry I can eat everything here! "
    + "Ce qui se passera aux calendes grecques. "
    + "That will happen when pigs fly. "
    + "Nous allons renvoyer aux calendes grecques la réunion. "
    + "We will postpone the meeting indefinitely.";
  
  @Test
  public void testStandalone() throws Exception {
    LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
      create(new StringReader(mixed));
    char[] filtered = new char[mixed.length()];
    filter.read(filtered, 0, mixed.length());
    filter.close();
    
    assertEquals("Filter broke the text?", new String(filtered), mixed);
    
    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertLangInOffsetRangeIs(langOffs, "fr",  0, 52);
    assertLangInOffsetRangeIs(langOffs, "en", 52, 94);
    assertLangInOffsetRangeIs(langOffs, "fr",  94, 135);
    assertLangInOffsetRangeIs(langOffs, "en", 135, 167);
    assertLangInOffsetRangeIs(langOffs, "fr", 167, 222);
    assertLangInOffsetRangeIs(langOffs, "en", 222, 264);
  }
  
  String moreMixed = "Brazil 2014 was the best World Cup ever. "
    + "وكانت البرازيل 2014 أفضل بطولة كأس العالم من أي وقت مضى. "
    + "巴西2014年是最好的世界杯永遠。"
    + "Бразилия 2014 был лучшим Кубка мира когда-либо. "
    + "브라질 2014 최고의 적 월드컵이었다. "
    + "Brésil 2014 a été la meilleure Coupe du Monde jamais.";
  
  /**
   * Test out Language offsets in a full analysis chain. Specifically:
   * <code>
   * TODO
   * </code>
   * @throws Exception
   */
  @Test
  public void testInAnalysisChain() throws Exception {
    Reader reader = new HTMLStripCharFilter(new StringReader(moreMixed));
    Reader withLangOffsets = new LangIdCharFilterFactory(NO_ARGS).create(reader);
    TokenStream tokenStream = new ICUTokenizer(withLangOffsets);
    tokenStream = new ICUFoldingFilter(tokenStream);
    
    List<List<String>> tokens = tokenToollbox.tokensAsList(tokenStream);
    
    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertLangInOffsetRangeIs(langOffs, "en",  0, 41);
    assertLangInOffsetRangeIs(langOffs, "ar", 41, 98);
    assertLangInOffsetRangeIs(langOffs, "zh-tw", 98, 115);
    assertLangInOffsetRangeIs(langOffs, "ru", 115, 163);
    assertLangInOffsetRangeIs(langOffs, "ko", 163, 186);
    assertLangInOffsetRangeIs(langOffs, "fr", 186, 239);
  }
  
  @Test
  public void testJoinsSameLanguageRuns() throws Exception {
    String english = "The quick brown fox jumps over the lazy dog. "
      + "But not always. Sometimes the dog gets the fox.";
    LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
      create(new StringReader(english));
    char[] filtered = new char[english.length()];
    filter.read(filtered, 0, english.length());
    filter.close();
    
    assertEquals("Filter broke the text?", new String(filtered), english);
    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertEquals("Multiple languages id'd?", 1, langOffs.getLanguages().size());
    assertEquals("Wrong language", "en", langOffs.getLanguages().iterator().next());
    assertEquals("Wrong final offset", new Integer(92), langOffs.keySet().iterator().next());
  }
}
