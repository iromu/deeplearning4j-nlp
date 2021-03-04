package stanford.nlp.classify;

import com.iromu.dl.nlp.DatasetNormalizer;
import com.iromu.dl.nlp.NoNormalizer;

public class ColumnDataClassifierNoNormTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierNoNorm.zip";
    private static final DatasetNormalizer NORMALIZER = new NoNormalizer();

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.target.trainClassifier();
        columnDataClassifierTrain.target.testClassifier();
    }

}

