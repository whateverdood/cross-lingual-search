package org.apache.lucene.analysis.polyglot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
import org.apache.lucene.analysis.polyglot.charfilter.LangIdCharFilterFactory;
import org.apache.lucene.analysis.polyglot.tokenattributes.LanguageAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

/**
 * Rudimentary test cases for driving a multi-lingual TokenStream through
 * the {@link PolyGlotStemmingTokenFilter}.
 *
 * @author rich
 */
public class SimplePolyglotStemmingTokenFilterTest {

  private static final HashMap<String, String> NO_ARGS = new HashMap<String, String>();
  String english = "The child laughed at the silly clown. ";
  String arabic = "انه ضحك علي مهرج. ";
  String hindi = "वह जोकर पर हँसे.";
  String multiLingualRun = english + arabic + hindi;
  
  @Test
  public void testMultilingualStemming() throws Exception {
    boolean dontPreserveOriginalToken = false;
    List<List<String>> output = tokenize(dontPreserveOriginalToken);
    
    System.out.println(String.format("Original: %s", multiLingualRun));
    System.out.println(String.format("Analyzed: %s", output.toString()));

    // "laughed" stems to "laugh"
    assertTrue("Output should contain token: laugh", contains("laugh", output));
    assertFalse("Output should not contain token: laughed", contains("laughed", output));

    // "على" stems to "عل"
    assertTrue("Output should contain token: عل", contains("عل", output));
    assertFalse("Output should not contain token: علي", contains("علي", output));
    
    // "जोकर" stems to "जो"
    assertTrue("Output should contain token: जो", contains("जो", output));
    assertFalse("Output should not contain token: जोकर", contains("जोकर", output));    
  }

  @Test
  public void testMultilingualStemmingPreservingOriginalTokens() throws Exception {
    boolean withOriginalToken = true;
    List<List<String>> output = tokenize(withOriginalToken);
    
    System.out.println(String.format("Original: %s", multiLingualRun));
    System.out.println(String.format("Analyzed: %s", output.toString()));

    // "laughed" stems to "laugh"
    assertTrue("Output should contain token: laugh", contains("laugh", output));
    assertTrue("Output should contain token: laughed", contains("laughed", output));

    // "على" stems to "عل"
    assertTrue("Output should contain token: عل", contains("عل", output));
    assertTrue("Output should contain token: علي", contains("علي", output));
    
    // "जोकर" stems to "जो"
    assertTrue("Output should contain token: जो", contains("जो", output));
    assertTrue("Output should contain token: जोकर", contains("जोकर", output));    
  }

  private List<List<String>> tokenize(boolean preserveOriginalToken) throws IOException {
    Reader r = new LangIdCharFilterFactory(NO_ARGS).create(
      new HTMLStripCharFilter(new StringReader(multiLingualRun)));
    TokenStream tokenStream = new ICUTokenizer(r);
    tokenStream = new ICUFoldingFilter(tokenStream);
    tokenStream = new LanguageMarkerFilter(tokenStream);
    tokenStream = new SimplePolyglotStemmingTokenFilter(tokenStream, preserveOriginalToken);
    
    CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posAttr = tokenStream.addAttribute(PositionIncrementAttribute.class);
    LanguageAttribute langAttr = tokenStream.addAttribute(LanguageAttribute.class);
    
    List<List<String>> output = new ArrayList<List<String>>(100);
    int i = 0;
    
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      List<String> tokenized = output.size() > i ? output.get(i) : null;
      if (posAttr.getPositionIncrement() == 0) {
        // back up b/c this token position hasn't incremented
        tokenized = output.get(i-1); 
      }
      if (tokenized == null) {
        tokenized = new ArrayList<String>();
        tokenized.add(langAttr.getLang());
        output.add(i, tokenized);
      }
      tokenized.add(termAttr.toString());
      i += posAttr.getPositionIncrement();
    }
    tokenStream.end();
    tokenStream.close();
    
    return output;
  }
  
  private boolean contains(String term, List<List<String>> collection) {
    for (List<String> terms : collection) {
      for (String t : terms) {
        if (t.equalsIgnoreCase(term)) {
          return true;
        }
      }
    }
    return false;
  }
  
}
