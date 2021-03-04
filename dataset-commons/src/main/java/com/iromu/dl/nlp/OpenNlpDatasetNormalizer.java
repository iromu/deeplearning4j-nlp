package com.iromu.dl.nlp;

import lombok.extern.slf4j.Slf4j;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class OpenNlpDatasetNormalizer implements DatasetNormalizer {
    DictionaryLemmatizer lemmatizer;
    POSTaggerME posTagger;
    SentenceDetectorME sentenceDetectorME;
    TokenizerME tokenizerME;

    public OpenNlpDatasetNormalizer() {
        try {
            TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/opennlp/models/en-token.bin"));
            tokenizerME = new TokenizerME(tokenizerModel);
            SentenceModel sentenceModel = new SentenceModel(getClass().getResourceAsStream("/opennlp/models/en-sent.bin"));
            sentenceDetectorME = new SentenceDetectorME(sentenceModel);
            POSModel posModel = new POSModel(getClass().getResourceAsStream("/opennlp/models/en-pos-maxent.bin"));
            posTagger = new POSTaggerME(posModel);
            lemmatizer = new DictionaryLemmatizer(getClass().getResourceAsStream("/opennlp/models/en-lemmatizer.dict"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String process(String paragraph) {
        StringBuilder builder = new StringBuilder();
        String[] sentences = sentenceDetectorME.sentDetect(paragraph);
        for (String sentence : sentences) {
            String[] tokens = tokenizerME.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);
            String[] lemmas = lemmatizer.lemmatize(tokens, tags);
            String collect = Arrays.stream(lemmas).filter(s -> !"O".equals(s)).collect(Collectors.joining(" "));
            builder.append(collect);
            builder.append(" ");
        }
        return builder.toString().trim().toLowerCase();
    }
}
