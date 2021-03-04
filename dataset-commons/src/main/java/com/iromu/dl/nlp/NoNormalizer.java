package com.iromu.dl.nlp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoNormalizer implements DatasetNormalizer {

    @Override
    public String process(String text) {
        return text;
    }

}
