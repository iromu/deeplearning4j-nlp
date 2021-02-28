package stanford.nlp.classify;

import com.iromu.dl4j.nlp.utils.DatasetNormalizer;

import java.nio.file.Paths;


public class ColumnDataClassifierNoNormSVMTrain {

    private static final String MODEL_NAME = "ColumnDataClassifierNoNormSVM.zip";
    private static final DatasetNormalizer NORMALIZER = null;
    private static final String SVM_MODEL_NAME = "ColumnDataClassifierNoNormSVM.svm";

    public static void main(String[] args) throws Exception {
        ColumnDataClassifierUtils columnDataClassifierTrain = new ColumnDataClassifierUtils(NORMALIZER, MODEL_NAME);
        columnDataClassifierTrain.getProps().setProperty("exitAfterTrainingFeaturization", "true");
        columnDataClassifierTrain.getProps().setProperty("printSVMLightFormatTo", Paths.get(columnDataClassifierTrain.OUTPUT_FOLDER, SVM_MODEL_NAME).toString());
        columnDataClassifierTrain.trainClassifier();

    }

}
