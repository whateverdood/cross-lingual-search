package sandbox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicStemmer;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeSource;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.ArmenianStemmer;
import org.tartarus.snowball.ext.BasqueStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class SimplePolyGlotStemmingTokenFilter extends TokenFilter {

  private AttributeSource.State currentState; // TODO: preserve original

  private ScriptAttribute scriptAttr;
  private CharTermAttribute termAttr;

  private static Map<String, Object> stemmers = new HashMap<String, Object>();
  static {
    try {
      DetectorFactory.loadProfile(new File(SimplePolyGlotStemmingTokenFilter.class
        .getResource("/profiles").toURI()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    stemmers.put("Latin", new ScriptStemmer() {
      Map<String, SnowballProgram> snowballs;

      SnowballProgram snowballFor(String lang) {
        if (snowballs == null) {
          snowballs = new HashMap<String, SnowballProgram>();
          snowballs.put("hy", new ArmenianStemmer());
          snowballs.put("en", new EnglishStemmer());
          snowballs.put("eu", new BasqueStemmer());
          snowballs.put("fr", new FrenchStemmer());
          snowballs.put("it", new ItalianStemmer());
          // ...
        }
        return snowballs.get(lang);
      }

      @Override
      public String stem(String term) {

        String lang = null;
        if (term.length() > 3) {
          // 3 is completely arbitrary, btw
          try {
            Detector detector = DetectorFactory.create();
            detector.append(term);
            lang = detector.detect();
            System.out.println(String.format("[%s] might be [%s]", term, lang));
          } catch (LangDetectException ignored) {
            ignored.printStackTrace();
          }
        }

        SnowballProgram stemmer = snowballFor(lang);
        if (null != stemmer) {
          stemmer.setCurrent(term);
          stemmer.stem();
          return stemmer.getCurrent();
        } else {
          return term;
        }
      }
    });
    stemmers.put("Arabic", new ScriptStemmer() {
      ArabicStemmer stemmer = new ArabicStemmer();

      @Override
      public String stem(String term) {
        char[] termBuffer = term.toCharArray();
        int stemmedLength = stemmer.stem(termBuffer, term.length());
        return new String(termBuffer, 0, stemmedLength);
      }
    });
  }

  protected SimplePolyGlotStemmingTokenFilter(TokenStream input) {
    super(input);

    scriptAttr = input.addAttribute(ScriptAttribute.class);
    termAttr = input.addAttribute(CharTermAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    boolean incremented = input.incrementToken();

    if (incremented) {
      stemToken();
    }

    return incremented;
  }

  private void stemToken() {
    String script = scriptAttr.getName();
    String term = termAttr.toString();

    System.out.println(String.format("(%s) [%s]", script, term));

    ScriptStemmer scriptStemmer = (ScriptStemmer) SimplePolyGlotStemmingTokenFilter.stemmers
      .get(script);
    if (null != scriptStemmer) {
      String stem = scriptStemmer.stem(term);
      if (!stem.equalsIgnoreCase(term)) {
        System.out.println(String.format("[%s] stemmed to [%s]", term, stem));
      }
      termAttr.resizeBuffer(stem.length());
      termAttr.copyBuffer(stem.toCharArray(), 0, stem.length());
    }
  }

}

interface ScriptStemmer {
  String stem(String term);
}
