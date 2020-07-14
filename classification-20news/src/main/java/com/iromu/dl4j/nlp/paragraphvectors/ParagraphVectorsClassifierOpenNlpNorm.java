package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl4j.nlp.utils.DatasetUtil;
import com.iromu.dl4j.nlp.utils.OpenNlpDatasetNormalizer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifierOpenNlpNorm {
    public static final String MODEL_NAME = "ParagraphVectorsClassifierOpenNlpNorm.zip";

    public static void main(String[] args) throws Exception {
        DatasetUtil.DatasetMeta datasetMeta = new DatasetUtil(new OpenNlpDatasetNormalizer()).prepareDataset();
        ParagraphVectorsClassifier app = new ParagraphVectorsClassifier(MODEL_NAME,datasetMeta);
        app.train();
        app.test();
    }
}
