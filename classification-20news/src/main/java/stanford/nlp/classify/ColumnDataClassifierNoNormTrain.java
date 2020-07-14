package stanford.nlp.classify;

import com.iromu.dl4j.nlp.utils.DatasetNormalizer;

public class ColumnDataClassifierNoNormTrain {

    private static String MODEL_NAME = "ColumnDataClassifierNoNorm.zip";
    private static DatasetNormalizer NORMALIZER = null;

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.trainClassifier();
        columnDataClassifierTrain.testClassifier();
    }

}
