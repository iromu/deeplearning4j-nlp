package com.iromu.dl4j.nlp.paragraphvectors;


import com.iromu.dl4j.nlp.utils.DatasetUtil;
import com.iromu.dl4j.nlp.utils.LabelSeeker;
import com.iromu.dl4j.nlp.utils.MeansBuilder;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Based on https://github.com/eclipse/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/nlp/paragraphvectors/ParagraphVectorsClassifier.java
 * <p>
 * SkipGram and DBOW
 *
 * @author raver119@gmail.com
 * @author wantez@gmail.com
 */
@Slf4j
public class ParagraphVectorsClassifier {

    public static String OUTPUT_FOLDER;
    private ParagraphVectors paragraphVectors;
    private FileLabelAwareIterator iterator;
    private TokenizerFactory tokenizerFactory;
    private final DatasetUtil.DatasetMeta datasetMeta;
    private final String MODEL_NAME;

    public ParagraphVectorsClassifier(String MODEL_NAME, DatasetUtil.DatasetMeta datasetMeta) throws URISyntaxException {
        OUTPUT_FOLDER = Paths.get(System.getProperty("user.home"), "datasets", "website-classification-models").toString();
        this.MODEL_NAME = MODEL_NAME;
        this.datasetMeta = datasetMeta;
    }

    void train() throws IOException {

        File locationToSave = getParagraphVectorFile();

        if (!locationToSave.exists()) {

            log.info("Training model");
            File resource = datasetMeta.getTrainPath().toFile();

            // build a iterator for our dataset
            iterator = new FileLabelAwareIterator.Builder()
                    .addSourceFolder(resource)
                    .build();

            tokenizerFactory = new DefaultTokenizerFactory();
            tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

            log.info("Workers: {}", Runtime.getRuntime().availableProcessors());

            // ParagraphVectors training configuration
            paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(0.025)
                    .minLearningRate(0.001)
                    .batchSize(1000)
                    .epochs(20)
                    .iterate(iterator)
                    .trainWordVectors(true)
                    .tokenizerFactory(tokenizerFactory)
                    //default .workers(Runtime.getRuntime().availableProcessors())
                    .build();

            // Start model training
            paragraphVectors.fit();

            WordVectorSerializer.writeParagraphVectors(paragraphVectors, locationToSave);
        } else {
            log.info("Loading model");
            paragraphVectors = WordVectorSerializer.readParagraphVectors(locationToSave);
        }
    }

    private File getParagraphVectorFile() {
        return new File(Paths.get(OUTPUT_FOLDER, MODEL_NAME).toString());
    }

    void test() {
        log.info("Testing");
      /*
      At this point we assume that we have model built and we can check
      which categories our unlabeled document falls into.
      So we'll start loading our unlabeled documents and checking them
     */
        File unClassifiedResource = datasetMeta.testPath.toFile();
        FileLabelAwareIterator unClassifiedIterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(unClassifiedResource)
                .build();

     /*
      Now we'll iterate over unlabeled data, and check which label it could be assigned to
      Please note: for many domains it's normal to have 1 document fall into few labels at once,
      with different "weight" for each.
     */
        MeansBuilder meansBuilder = new MeansBuilder(
                (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
                tokenizerFactory);
        LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
                (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());
        int total = 0;
        long start = System.currentTimeMillis();
        int errors = 0;
        while (unClassifiedIterator.hasNextDocument()) {
            total++;
            LabelledDocument document = unClassifiedIterator.nextDocument();
            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);

         /*
          please note, document.getLabel() is used just to show which document we're looking at now,
          as a substitute for printing out the whole document name.
          So, labels on these two documents are used like titles,
          just to visualize our classification done properly
         */

            String expected = document.getLabels().get(0);
            Pair<String, Double> max = getMaxScore(scores);
            //log.info("Document '{}' labeled as '{}' with an score of '{}'", expected, max.getKey(), max.getValue());
            if (!expected.equals(max.getKey())) errors++;
        }
        log.info("Test Errors {}/{} {}% {}ms", errors, total, (errors * 100 / total), System.currentTimeMillis() - start);
    }

    public static Pair<String, Double> getMaxScore(List<Pair<String, Double>> scores) {
        Pair<String, Double> maxValue = new Pair<>(null, Double.MIN_VALUE);
        for (Pair<String, Double> score : scores) {
            if (score.getValue() > maxValue.getValue()) {
                maxValue = score;
            }
        }
        return maxValue;
    }
}
