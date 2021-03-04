package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl.nlp.DatasetMeta;
import com.iromu.dl.nlp.DatasetUtil;
import com.iromu.dl.nlp.NoNormalizer;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

import static com.iromu.dl.nlp.DatasetPath.getRootPath;

/**
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifierNoNorm {
    public static final String MODEL_NAME = "ParagraphVectorsClassifierNoNorm.zip";

    public static void main(String[] args) throws Exception {
        DatasetMeta datasetMeta = new DatasetUtil(new NoNormalizer(),
                Paths.get(getRootPath(), "datasets", "URL Classification", "parsed"),
                Paths.get(getRootPath(), "datasets", "website-classification-workdir")).prepareDataset();
        ParagraphVectorsClassifier app = new ParagraphVectorsClassifier(MODEL_NAME, datasetMeta);
        app.train();
        app.test();
    }
}
