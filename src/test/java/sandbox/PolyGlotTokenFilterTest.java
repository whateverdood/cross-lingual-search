package sandbox;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import sandbox.SimplePolyGlotStemmingTokenFilter;

public class PolyGlotTokenFilterTest {

  String english = "he laughed at the clown";
  String arabic = "انه ضحك على مهرج";
  String chinese_tr = "他笑的小丑";
  String italian = "rideva il clown";
  String multiLingualRun = english + " " + arabic + " " + chinese_tr + " " + italian;
  
  @Test
  public void testMultilingualText() throws Exception {
    boolean dontPreserveOriginalToken = false;
    String output = tokenizeMultiLingualRun(dontPreserveOriginalToken);
    
    System.out.println(String.format("Original: %s", multiLingualRun));
    System.out.println(String.format("Analyzed: %s", output));

    // "laughed" stems to "laugh"
    assertTrue("Output should contain token: laugh", output.contains("laugh"));
    assertFalse("Output should not contain token: laughed", output.contains("laughed"));

    // "على" stems to "عل"
    assertTrue("Output should contain token: عل", output.contains("عل"));
    assertFalse("Output should not contain token: على", output.contains("على"));
  }

  @Test
  public void testMultilingualTextPreservingOriginalTokens() throws Exception {
    boolean withOriginalToken = true;
    String output = tokenizeMultiLingualRun(withOriginalToken);
    
    System.out.println(String.format("Original: %s", multiLingualRun));
    System.out.println(String.format("Analyzed: %s", output));

    // "laughed" stems to "laugh"
    assertTrue("Output should contain token: laugh", output.contains("laugh"));
    assertTrue("Output should contain token: laughed", output.contains("laughed"));

    // "على" stems to "عل"
    assertTrue("Output should contain token: عل", output.contains("عل"));
    assertTrue("Output should contain token: على", output.contains("على"));
  }

  private String tokenizeMultiLingualRun(boolean preserveOriginalToken) throws IOException {
    StringBuilder output = new StringBuilder();
    TokenStream tokenStream = new ICUTokenizer(new StringReader(multiLingualRun));
    tokenStream = new ICUFoldingFilter(tokenStream);
    tokenStream = new SimplePolyGlotStemmingTokenFilter(tokenStream, preserveOriginalToken);
    CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);

    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      output.append(termAttr.toString());
      output.append(',');
    }
    tokenStream.end();
    tokenStream.close();
    
    return output.toString();
  }
  
}
