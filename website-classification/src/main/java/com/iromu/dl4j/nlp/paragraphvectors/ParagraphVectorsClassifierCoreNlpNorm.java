package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl.nlp.CoreNlpDatasetNormalizer;
import com.iromu.dl.nlp.DatasetMeta;
import com.iromu.dl.nlp.DatasetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

import static com.iromu.dl.nlp.DatasetPath.getRootPath;

/**
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifierCoreNlpNorm {
    public static final String MODEL_NAME = "ParagraphVectorsClassifierCoreNlpNorm.zip";

    public static void main(String[] args) throws Exception {
        DatasetMeta datasetMeta = new DatasetUtil(new CoreNlpDatasetNormalizer(),
                Paths.get(getRootPath(), "datasets", "URL Classification", "parsed"),
                Paths.get(getRootPath(), "datasets", "website-classification-workdir")
        ).prepareDataset();
        ParagraphVectorsClassifier app = new ParagraphVectorsClassifier(MODEL_NAME, datasetMeta);
        app.train();
        app.test();
    }
}
