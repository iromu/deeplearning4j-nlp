package stanford.nlp.classify;


import com.iromu.dl.nlp.DatasetNormalizer;

import java.nio.file.Paths;


public class ColumnDataClassifierNoNormSVMTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierNoNormSVM.zip";
    private static final DatasetNormalizer NORMALIZER = null;
    private static final String SVM_MODEL_NAME = "ColumnDataClassifierNoNormSVM.svm";

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.target.getProps().setProperty("exitAfterTrainingFeaturization", "true");
        columnDataClassifierTrain.target.getProps().setProperty("printSVMLightFormatTo", Paths.get(columnDataClassifierTrain.target.OUTPUT_FOLDER, SVM_MODEL_NAME).toString());
        columnDataClassifierTrain.target.trainClassifier();

    }

}
