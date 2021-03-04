package stanford.nlp.classify;

import com.iromu.dl.nlp.DatasetNormalizer;
import com.iromu.dl.nlp.NoNormalizer;

public class ColumnDataClassifierNoNormNBTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierNoNormNB.zip";
    private static final DatasetNormalizer NORMALIZER = new NoNormalizer();

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.target.getProps().setProperty("useNB", "true");
        columnDataClassifierTrain.target.trainClassifier();
        columnDataClassifierTrain.target.testClassifier();
    }

}
