package org.apache.lucene.analysis.polyglot.tokenattributes;

import org.apache.lucene.util.Attribute;

/**
 * This attribute stores the tokens ISO-639-1 language code.  
  */
public interface LanguageAttribute extends Attribute {

  public void setLang(String lang);
  
  public String getLang();
  
}
