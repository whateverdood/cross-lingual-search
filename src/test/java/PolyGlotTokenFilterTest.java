import static org.junit.Assert.*;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class PolyGlotTokenFilterTest {

    @Test
    public void testMultilingualText() throws Exception {
        
        String english = "he laughed at the clown";
        String arabic = "انه ضحك على مهرج";
        String chinese_tr = "他笑的小丑";
        String italian = "rideva il clown";
        String multiLingualRun = english + " " + arabic + " " + chinese_tr + " " + italian;
        
        TokenStream tokenStream = new ICUTokenizer(new StringReader(multiLingualRun));
        tokenStream = new ICUFoldingFilter(tokenStream);
        tokenStream = new SimplePolyGlotStemmingTokenFilter(tokenStream);
        CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
        
        tokenStream.reset();
        StringBuilder sb = new StringBuilder();
        while (tokenStream.incrementToken()) {
            sb.append(termAttr.toString());
            sb.append(',');
        }
        tokenStream.end();
        tokenStream.close();

        // "laughed" stems to "laugh"
        assertFalse(sb.toString().contains("laughed"));
        assertTrue(sb.toString().contains("laugh"));

        // "على" stems to "عل"
        assertFalse(sb.toString().contains("على"));
        assertTrue(sb.toString().contains("عل"));
        
        System.out.println(String.format("Original: %s", multiLingualRun));
        System.out.println(String.format("Analyzed: %s", sb.toString()));
    }

}
