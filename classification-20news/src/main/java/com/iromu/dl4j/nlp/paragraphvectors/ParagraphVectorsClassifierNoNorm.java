package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl.nlp.DatasetMeta;
import com.iromu.dl.nlp.DatasetUtil;
import com.iromu.dl.nlp.NoNormalizer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifierNoNorm {
    public static final String MODEL_NAME = "ParagraphVectorsClassifierNoNorm.zip";

    public static void main(String[] args) throws Exception {
        DatasetMeta datasetMeta = new DatasetUtil(new NoNormalizer(), "20news-18828", "20news-18828-workdir").prepareDataset();
        ParagraphVectorsClassifier app = new ParagraphVectorsClassifier(MODEL_NAME, datasetMeta);
        app.train();
        app.test();
    }
}
