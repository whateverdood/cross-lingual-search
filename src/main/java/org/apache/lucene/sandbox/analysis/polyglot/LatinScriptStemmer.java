package org.apache.lucene.sandbox.analysis.polyglot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.ArmenianStemmer;
import org.tartarus.snowball.ext.BasqueStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LatinScriptStemmer implements ScriptStemmer {
  
  static {
    try {
      DetectorFactory.loadProfile(new File(SimplePolyGlotStemmingTokenFilter.class
        .getResource("/profiles").toURI()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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

    String lang = "en";
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

}
