package org.apache.lucene.analysis.polyglot.charfilter;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.polyglot.TokenToolbox;
import org.apache.lucene.analysis.polyglot.charfilter.LangIdCharFilter.LanguageOffsets;
import org.junit.Test;

public class LangIdCharFilterTest {
  
  public static final Map<String, String> NO_ARGS = new HashMap<String, String>();

  private void assertLangInOffsetRangeIs(
    LanguageOffsets langOffsets, String lang, int start, int end) {
    for (int i = start; i < end; i++) {
      assertTextAtOffsetIs(langOffsets, lang, i);
    }
  }

  private void assertTextAtOffsetIs(LanguageOffsets langOffs, String lang, int i) {
    assertEquals(String.format("Wrong lang [%s] at offset [%d]", lang, i), 
      lang, langOffs.langAtOffset(i));
  }
  
  TokenToolbox tokenToolbox = new TokenToolbox();
  
  @Test
  public void testStandalone() throws Exception {
    LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
      create(new StringReader(TokenToolbox.mixed));
    char[] filtered = new char[TokenToolbox.mixed.length()];
    filter.read(filtered, 0, TokenToolbox.mixed.length());
    filter.close();
    
    assertEquals("Filter broke the text?", new String(filtered), TokenToolbox.mixed);
    
    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertLangInOffsetRangeIs(langOffs, "fr",  0, 52);
    assertLangInOffsetRangeIs(langOffs, "en", 52, 94);
    assertLangInOffsetRangeIs(langOffs, "fr",  94, 135);
    assertLangInOffsetRangeIs(langOffs, "en", 135, 167);
    assertLangInOffsetRangeIs(langOffs, "fr", 167, 222);
    assertLangInOffsetRangeIs(langOffs, "en", 222, 264);
  }
  
  /**
   * Test out Language offsets in a full analysis chain. Specifically:
   * <code>
   * TODO
   * </code>
   * @throws Exception
   */
  @Test
  public void testInAnalysisChain() throws Exception {
    LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
      create(new StringReader(TokenToolbox.moreMixed));
    char[] filtered = new char[TokenToolbox.moreMixed.length()];
    filter.read(filtered, 0, TokenToolbox.moreMixed.length());
    filter.close();

    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertLangInOffsetRangeIs(langOffs, "en",  0, 41);
    assertLangInOffsetRangeIs(langOffs, "ar", 41, 98);
    assertLangInOffsetRangeIs(langOffs, "zh-tw", 98, 115);
    assertLangInOffsetRangeIs(langOffs, "ru", 115, 163);
    assertLangInOffsetRangeIs(langOffs, "ko", 163, 186);
    assertLangInOffsetRangeIs(langOffs, "fr", 186, 239);
  }
  
  @Test
  public void testJoinsSameLanguageRuns() throws Exception {
    String english = "The quick brown fox jumps over the lazy dog. "
      + "But not always. Sometimes the dog gets the fox.";
    LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
      create(new StringReader(english));
    char[] filtered = new char[english.length()];
    filter.read(filtered, 0, english.length());
    filter.close();
    
    assertEquals("Filter broke the text?", new String(filtered), english);
    LanguageOffsets langOffs = LangIdCharFilter.getLanguageOffsets();
    assertEquals("Multiple languages id'd?", 1, langOffs.getLanguages().size());
    assertEquals("Wrong language", "en", langOffs.getLanguages().iterator().next());
    assertEquals("Wrong final offset", new Integer(92), langOffs.keySet().iterator().next());
  }
  
  String text = "Citation analysis, also called bibliometrics, a major part of my project, has been used to obtain many different kinds of information. Examination of citation practices creates a large, complex web of interaction which can reveal otherwise undetectable trends. For example, co-citations have been used to map out specializations of scientific fields. Co- citation analysis is a specific kind of citation analysis, in which the similarity between two articles A and B is measured by the number of articles have cited both A and B. Since both A and B are referenced by the same article, their content is linked in some way. This is in contrast to another form of citation analysis, bibliometric coupling, which the similarity between two articles A and B is measured by the number of references shared by A and B. " + 
    "An important investigation of co-citation networks was described by Henry Small in his 1999 paper Crossing Disciplinary Boundaries. Although Small acknowledged past " + 
    "research on linking papers by shared vocabulary or index terms, he found that citations “represent(ed) a more direct author-selected dependency,” and therefore made a strong foundation for study of inter-textual relationships. This observation motivated my own decision to focus on citation data. " + 
    "Small had previously observed that citations tended to concentrate “in narrowly defined pockets that correspond roughly to specialties or invisible colleges of researchers.” (1974). Researchers within a concise specialty would tend to cite the works of others within that specialty, creating small, interconnected groups of articles that were identifiable as a specialty. In his 1999 paper, Small examined articles that through their citations created a link between one group and another, linking the specialization groups. He observed that the articles drawing on citations from outside their specialty may be introducing an innovation. This could allow us to pinpoint where ideas cross from one field to another. In this and many other ways, examination of citations can reveal new and useful information about the relationships between articles and the ideas within them. " + 
    "Braam (1991) examined the combination of citation maps with word profiles from a collection of articles and abstracts. This dataset consisted of abstracts from 3400 agricultural publications in Chemical Abstracts, and an additional 1384 publications on chemoreception from BIOSIS. These articles were combined with citation data from ISI’s Science Citation Index. The citation maps and word profiles of these documents gave two images of the same dataset. Braam compared term frequencies from the abstracts with clustered co-citation analysis, examining how these profiles changed each year. This " + 
    "revealed new details, such as the development and adoption of specialized terminology within that discipline. Braam notes that when a specialization is unstable, developing quickly, the articles that get cited will vary more widely. In this situation, world profiles may be preferable to citations in establishing specialization groups. Contrasting word profiles with citation analysis gave a new way to track the development of ideas within scientific specialization. " + 
    "Chen used citation analysis to trace the diffusion of knowledge through fields of science. (Chen, 2004). In this case, knowledge refers to the adoption of new concepts or processes by later writers who cite the earlier writers. Chen coupled citation analysis with other techniques- in this case, network theory and network visualization were applied to the network of citations. Visualization has turned out to be a very useful tool for assessing clusters in the network, as the results are far easier to understand visually. Chen’s visualizations show tight clusters, interlinked by points of diffusion where articles (or in this case, patents) cite outside the clustered group. " + 
    "Mapping science is not the only goal of citation analysis. Citations have also been used as measures of similarity, in various ways. Giles and his colleagues (1999) developed a measure of similarity that depended on common citations between articles, without reference to the text in the article. If two articles cite the many of the same sources, it indicated a degree of similarity in content between those two articles. He thereby proposed an alternative to TFIDF scores. However, this method can only be applied on " + 
    "the level of the whole article, so TFIDF remains a viable choice for determining similarity within the article. " + 
    "A recent article (Elkis, 2008) entitled Blind Men and Elephants discussed the kinds of information can be discerned about an article by considering citation summaries. They concluded that when two articles were co-cited by the same article, those two articles would tend to be similar, and that this similarity increased with the proximity of the two citations within the citing article. This new measure of similarity was compared with tf- idf cosine similarity scores for the same text, and the two were found to perform very much alike, ranking items in close to the same order. Consequently, Elkis proposes, co- citation may be used as a measure of similarity. This article also examined self-cohesion, a measure of similarity between the sentences within a article, and cross-cohesion, similarity between that article and some other entity- in this case, the article’s abstract, and the collection of sentences citing that article. The sentences citing an article are generally more similar to the article’s main text than the article’s own abstract. Abstracts and citing sentences have different characteristics- for instance, the abstract covers the overall content in an article, while the citing sentences often focus on portions of the same article, and may not cover all of the content within. However, Elkis suggests that in the absence of an abstract, citing sentences may profitably replace it, through a process of automatic summarization. " + 
    "Wangzhong (2006) proposed a measure of similarity based on citation linking through a graph, using two algorithms- the maximum flow metric and the authority vector metric. " + 
    "Wangzhong concluded that citation and text based analysis are useful complements, confirming Braam’s prior observations. " + 
    "Klavans (2006) discusses several measures of similarity (or relatedness), such as the Pearson correlation. While he notes that little research has been done previously to evaluate the accuracy of relatedness measures, Klavans concludes that the cosine index performs the best. Van Eck (2009) recently reached a similar conclusion after comparing several techniques for measurement of similarity. Their observations helped to inform my use of cosine similarity measures in this project. " + 
    "Ritchie (2008) performed a series of experiments in which the retrieval performance of the terms from within a document was compared with the terms used to describe that document in citations combined with the terms from within the document. It was discovered that adding the terms from the citations improved overall information retrieval performance. While these experiments were just the beginning of a longer term research project, they already support the idea that citation text has many more possibilities that we’ve not yet tapped.";
  
  @Test
  public void testLots() throws Exception {
    long started = System.currentTimeMillis();
    int iterations = 1000;
    for (int i = 0; i < iterations; i++) {
      LangIdCharFilter filter = (LangIdCharFilter) new LangIdCharFilterFactory(NO_ARGS).
        create(new StringReader(text));
      char[] filtered = new char[text.length()];
      filter.read(filtered, 0, text.length());
      filter.close();
    }
    long elapsed = System.currentTimeMillis() - started;
    System.out.println(String.format("%.2f per sec", (float)(iterations / (elapsed/1000))));
  }
}
