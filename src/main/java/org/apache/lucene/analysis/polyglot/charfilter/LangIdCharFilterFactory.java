package org.apache.lucene.analysis.polyglot.charfilter;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import org.apache.lucene.analysis.util.CharFilterFactory;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Factory for {@link LangIdCharFilter}. Uses the Cybozu Labs language
 * identification toolkit, which requires language profile data.
 * <code>
 * &lt;charFilter 
 *   class="org.apache.lucene.analysis.polyglot.LangIdCharFilterFactory"
 *   languageProfilesDirectory="/path/to/profiles"/&gt;
 * </code>
 */
public class LangIdCharFilterFactory extends CharFilterFactory {
  
  private static final String LANGUAGE_PROFILES_DIRECTORY = "languageProfilesDirectory";
  private final static String DEFAULT_LANGUAGE_PROFILE_LOCATION = "/languageProfiles";

  public LangIdCharFilterFactory(Map<String, String> args) {
    super(args);
    try {
      initialize(get(args, LANGUAGE_PROFILES_DIRECTORY, DEFAULT_LANGUAGE_PROFILE_LOCATION));
    } catch (IllegalArgumentException e) {
      initialize(DEFAULT_LANGUAGE_PROFILE_LOCATION);
    }
  }
  
  private static synchronized void initialize(String langProfileDirectory) 
    throws IllegalArgumentException {
    if (DetectorFactory.getLangList() == null || DetectorFactory.getLangList().size() == 0) {
      try {
        DetectorFactory.clear();
        if (new File(langProfileDirectory).exists()) {
          DetectorFactory.loadProfile(new File(langProfileDirectory));
        } else {
          URI uri = LangIdCharFilterFactory.class.getResource(langProfileDirectory).toURI();
          DetectorFactory.loadProfile(new File(uri));
        }
      } catch (LangDetectException e) {
        throw new IllegalArgumentException(
          String.format("Couldn't initialize using [%s] because [%s], code [%s]",
            langProfileDirectory, e.getMessage(), e.getCode()), e);
      } catch (Exception e) {
        throw new IllegalArgumentException(
          String.format("Couldn't initialize using [%s] because [%s]",
            langProfileDirectory, e.getMessage()), e);        
      }
    }
  }

  @Override
  public Reader create(Reader reader) {
    return new LangIdCharFilter(reader);
  }

}
