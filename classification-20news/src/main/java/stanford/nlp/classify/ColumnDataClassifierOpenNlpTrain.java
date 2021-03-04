package stanford.nlp.classify;

import com.iromu.dl.nlp.DatasetNormalizer;
import com.iromu.dl.nlp.OpenNlpDatasetNormalizer;

public class ColumnDataClassifierOpenNlpTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierOpenNlp.zip";
    private static final DatasetNormalizer NORMALIZER = new OpenNlpDatasetNormalizer();

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.target.trainClassifier();
        columnDataClassifierTrain.target.testClassifier();
    }

}
