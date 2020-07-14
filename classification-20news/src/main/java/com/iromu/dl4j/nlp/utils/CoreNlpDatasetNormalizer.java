package com.iromu.dl4j.nlp.utils;

import edu.stanford.nlp.process.TokenizerFactory;
import lombok.extern.slf4j.Slf4j;

import edu.stanford.nlp.pipeline.*;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class CoreNlpDatasetNormalizer implements DatasetNormalizer {
    final StanfordCoreNLP pipeline;
    private static final Pattern punctPattern = Pattern.compile("[\\d.:,\"'()\\[\\]|/?!;><]+");

    public CoreNlpDatasetNormalizer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public String process(String text) {

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        StringBuilder builder = new StringBuilder();
        for (CoreSentence sentence : document.sentences()) {
            List<String> lemmas = sentence.lemmas();
            List<String> nerTags = sentence.nerTags();
            int i = 0;
            for (String lemma : lemmas) {
                String token;
                if ("O".equals(nerTags.get(i))) {
                    token = replace(lemma);
                } else {
                    token = nerTags.get(i);
                }
                if (!token.isBlank()) {
                    builder.append(token);
                    builder.append(" ");
                }
                i++;
            }
        }
        return builder.toString().trim().toLowerCase();
    }

    private String replace(String base) {
        return punctPattern.matcher(base).replaceAll("").trim();
    }
}
