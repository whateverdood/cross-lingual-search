package org.apache.lucene.analysis.polyglot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class TokenToolbox {

  public List<List<String>> tokensAsList(TokenStream tokens) throws IOException {
    List<List<String>> list = new ArrayList<List<String>>(100);
    int i = 0;
    CharTermAttribute termAttr = tokens.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posAttr = tokens.addAttribute(PositionIncrementAttribute.class);
    OffsetAttribute offAttr = tokens.addAttribute(OffsetAttribute.class);
    
    tokens.reset();
    while (tokens.incrementToken()) {
      List<String> tokenized = list.size() > i ? list.get(i) : null;
      if (posAttr.getPositionIncrement() == 0) { 
        // backup
        tokenized = list.get(i - 1);
      }
      if (tokenized == null) {
        tokenized = new ArrayList<String>();
        list.add(i, tokenized);
      }
      tokenized.add(String.format("%s:%d..%d", termAttr.toString(), 
        offAttr.startOffset(), offAttr.endOffset()));
      i += posAttr.getPositionIncrement();
    }
    tokens.end();
    tokens.close();
    return list;
  }
  
  public boolean listContainsTerm(List<List<String>> list, String term) {
    for (List<String> terms: list) {
      for (String t: terms) {
        if (t.contains(term + ":")) {
          return true;
        }
      }
    }
    return false;
  }
}
