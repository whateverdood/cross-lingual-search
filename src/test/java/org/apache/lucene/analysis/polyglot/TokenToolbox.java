package org.apache.lucene.analysis.polyglot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class TokenToolbox {

  public static String mixed = 
    "Je peux manger tout cela ici, j'ai un faim de loup! "
    + "I am so hungry I can eat everything here! "
    + "Ce qui se passera aux calendes grecques. "        
    + "That will happen when pigs fly. "
    + "Nous allons renvoyer aux calendes grecques la réunion. "
    + "We will postpone the meeting indefinitely.";

  public static String moreMixed = 
    "Brazil 2014 was the best World Cup ever. "                    // en
    + "وكانت البرازيل 2014 أفضل بطولة كأس العالم من أي وقت مضى. "  // ar
    + "巴西2014年是最好的世界杯永遠。"                                  // zh-tw
    + "Бразилия 2014 был лучшим Кубка мира когда-либо. "          // ru
    + "브라질 2014 최고의 적 월드컵이었다. "                             // ko
    + "Brésil 2014 a été la meilleure Coupe du Monde jamais.";    // fr
  
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
