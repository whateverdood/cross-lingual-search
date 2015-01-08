package org.apache.lucene.analysis.polyglot.charfilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.analysis.charfilter.BaseCharFilter;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.ibm.icu.text.BreakIterator;

/**
 * A CharFilter that wraps another Reader and tries to identify the language
 * of the character stream. Identified languages and their last offset position
 * in the stream are captured in a ThreadLocal for use by downstream
 * analysis components. 
 */
public class LangIdCharFilter extends BaseCharFilter {
    
  private static ThreadLocal<LanguageOffsets> languageOffsets = new ThreadLocal<LanguageOffsets>() {
    protected LanguageOffsets initialValue() {
      return new LangIdCharFilter.LanguageOffsets();
    };
  };

  public static LanguageOffsets getLanguageOffsets() {
    return languageOffsets.get();
  }

  private Detector detector;
  private int minSentenceLength = 10;
  
  public LangIdCharFilter(Reader in) {
    super(in);
    try {
      detector = DetectorFactory.create();
    } catch (LangDetectException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int i = 0;
    for (i = 0; i < len; i++) {
      int ch = input.read();
      if (ch == -1) {
        break;
      }
      cbuf[off++] = (char) ch;
    }
    if (i == 0) {
      if (len == 0) {
        return 0;
      }
      return -1;
    }
    langIdSentences(new String(cbuf));
    return i;
  }
  
  private void langIdSentences(String text) {
    BreakIterator sentenceBreakIterator = BreakIterator.getSentenceInstance();
    sentenceBreakIterator.setText(text);
    
    Map<Integer, String> langPos = new LinkedHashMap<Integer, String>();
    
    int start = sentenceBreakIterator.first();
    for (int end = sentenceBreakIterator.next(); end != BreakIterator.DONE; 
      start = end, end = sentenceBreakIterator.next()) {
      
      String sentence = text.substring(start, end);
      if (null != sentence && !"".equals(sentence.trim())) {
        if (sentence.length() >= minSentenceLength) {          
          try {
            detector.setText(sentence);
            String lang = detector.detect();
            langPos.put(end, lang);
          } catch (LangDetectException e) {
            // TODO: handle or ignore?
          }
        } else {
          langPos.put(end, "N/A");          
        }
      }
    }
    LanguageOffsets langOffsets = languageOffsets.get();
    langOffsets.clear();
    langOffsets.putAll(joinSameLanguageOffsetRuns(langPos));
  }
  
  private Map<Integer, String> joinSameLanguageOffsetRuns(Map<Integer, String> langPos) {
    Map<Integer, String> joined = new LinkedHashMap<Integer, String>();
    if (langPos.size() > 0) {
      int lastOffset = langPos.keySet().iterator().next();
      String language = langPos.get(lastOffset);
      for (Integer offset: langPos.keySet()) {
        if (!language.equalsIgnoreCase(langPos.get(offset))) {
          joined.put(lastOffset, language);
        }
        lastOffset = offset;
        language = langPos.get(lastOffset);
      } 
      joined.put(lastOffset, language);
    }
    return joined;
  }

  /**
   * LanguageOffsets is an ordered Map of identified languages, keyed by 
   * the last position of each continuous run of language text.
   */
  @SuppressWarnings("serial")
  public static class LanguageOffsets extends LinkedHashMap<Integer, String> {
    public String langAtOffset(int i) {
      for (Integer end: keySet()) {
        if (i < end) {
          return get(end);
        }
      }
      return "en"; // TODO: default lang should be configurable
    }
    
    public Collection<String> getLanguages() {
      return values();
    }
  }

}
