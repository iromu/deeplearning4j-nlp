package stanford.nlp.classify;

import com.iromu.dl4j.nlp.utils.CoreNlpDatasetNormalizer;
import com.iromu.dl4j.nlp.utils.DatasetNormalizer;

public class ColumnDataClassifierCoreNlpTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierCoreNlp.zip";
    private static final DatasetNormalizer NORMALIZER = new CoreNlpDatasetNormalizer();

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.trainClassifier();
        columnDataClassifierTrain.testClassifier();
    }

}
