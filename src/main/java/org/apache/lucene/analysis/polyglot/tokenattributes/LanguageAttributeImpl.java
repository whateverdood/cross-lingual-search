package org.apache.lucene.analysis.polyglot.tokenattributes;

import org.apache.lucene.util.AttributeImpl;

/**
 * Implementation that stores the token language code as a String. 
 */
public class LanguageAttributeImpl extends AttributeImpl implements LanguageAttribute {
  
  private String lang;

  @Override
  public void setLang(String lang) {
    this.lang = new String(lang);
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public void clear() {
    this.lang = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    LanguageAttribute attr = (LanguageAttribute) target;
    attr.setLang(getLang());
  }
  
}
