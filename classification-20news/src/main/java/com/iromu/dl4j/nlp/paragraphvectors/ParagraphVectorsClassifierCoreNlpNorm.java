package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl.nlp.CoreNlpDatasetNormalizer;
import com.iromu.dl.nlp.DatasetMeta;
import com.iromu.dl.nlp.DatasetUtil;
import lombok.extern.slf4j.Slf4j;
/**
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifierCoreNlpNorm  {
    public static final String MODEL_NAME = "ParagraphVectorsClassifierCoreNlpNorm.zip";

    public static void main(String[] args) throws Exception {
        DatasetMeta datasetMeta = new DatasetUtil(new CoreNlpDatasetNormalizer(), "20news-18828", "20news-18828-workdir").prepareDataset();
        ParagraphVectorsClassifier app = new ParagraphVectorsClassifier(MODEL_NAME, datasetMeta);
        app.train();
        app.test();
    }
}
