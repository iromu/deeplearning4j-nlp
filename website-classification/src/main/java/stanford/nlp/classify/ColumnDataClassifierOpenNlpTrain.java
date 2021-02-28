package stanford.nlp.classify;

import com.iromu.dl4j.nlp.utils.DatasetNormalizer;
import com.iromu.dl4j.nlp.utils.OpenNlpDatasetNormalizer;

public class ColumnDataClassifierOpenNlpTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierOpenNlp.zip";
    private static final DatasetNormalizer NORMALIZER = new OpenNlpDatasetNormalizer();

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.trainClassifier();
        columnDataClassifierTrain.testClassifier();
    }

}
