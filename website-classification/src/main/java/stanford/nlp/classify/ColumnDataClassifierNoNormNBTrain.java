package stanford.nlp.classify;

import com.iromu.dl4j.nlp.utils.DatasetNormalizer;

public class ColumnDataClassifierNoNormNBTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierNoNormNB.zip";
    private static final DatasetNormalizer NORMALIZER = null;

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.getProps().setProperty("useNB", "true");
        columnDataClassifierTrain.trainClassifier();
        columnDataClassifierTrain.testClassifier();
    }

}