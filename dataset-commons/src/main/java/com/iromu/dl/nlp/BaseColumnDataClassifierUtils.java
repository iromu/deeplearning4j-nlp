package com.iromu.dl.nlp;

import edu.stanford.nlp.classify.ColumnDataClassifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class BaseColumnDataClassifierUtils {
    public final String OUTPUT_FOLDER;
    /**
     * Check for property options here
     * https://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/edu/stanford/nlp/classify/ColumnDataClassifier.html
     */
    private final Properties props;
    private final String modelName;
    private ColumnDataClassifier columnDataClassifier;

    public BaseColumnDataClassifierUtils(DatasetNormalizer normalizer, String modelName, Path origin, Path workdir) throws IOException, ClassNotFoundException {
        this.modelName = modelName;
        OUTPUT_FOLDER = workdir.toString();
        props = new Properties();

        DatasetUtil datasetUtil = new DatasetUtil(normalizer, origin, workdir);
        props.setProperty("trainFile", datasetUtil.workingPath.resolve("stanford-classifier.train").toString());
        props.setProperty("testFile", datasetUtil.workingPath.resolve("stanford-classifier.test").toString());
        props.setProperty("2.useSplitWords", "true");
        props.setProperty("2.splitWordsRegexp", "\\s+");
        props.setProperty("printFeatures", "prop1");

        datasetUtil.stanford();
        readModel();
    }

    public void readModel() throws IOException, ClassNotFoundException {
        if (Paths.get(OUTPUT_FOLDER, modelName).toFile().exists()) {
            //to read the file you need a temp buffer or file to decompress the archive
            File temp = Files.createTempFile("", "").toFile();
            log.info("Unzipping to {}", temp.toURI().toString());
            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(Paths.get(OUTPUT_FOLDER, modelName).toFile())));
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
                 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(temp))) {
                zis.getNextEntry();
                IOUtils.copy(zis, bos);
                bos.close();
                ObjectInputStream ois = new ObjectInputStream(bis);
                columnDataClassifier = ColumnDataClassifier.getClassifier(ois);
            }

            temp.delete();
        }
    }

    public Properties getProps() {
        return props;
    }

    public void trainClassifier() throws IOException {
        if (!Paths.get(OUTPUT_FOLDER, modelName).toFile().exists()) {
            log.info("Training {}", modelName);
            columnDataClassifier = new ColumnDataClassifier(props);
            columnDataClassifier.trainClassifier(props.get("trainFile").toString());
            if (!props.containsKey("exitAfterTrainingFeaturization"))
                try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(Paths.get(OUTPUT_FOLDER, modelName).toFile())))) {
                    zipStream.putNextEntry(new ZipEntry(modelName.replace(".zip", ".ser")));
                    ObjectOutputStream objectStream = new ObjectOutputStream(zipStream);
                    columnDataClassifier.serializeClassifier(objectStream);
                }
        }
    }

    public void testClassifier() throws IOException {
        if (!Paths.get(OUTPUT_FOLDER, modelName).toFile().exists()) {
            trainClassifier();
        }
        if (!props.containsKey("exitAfterTrainingFeaturization"))
            columnDataClassifier.testClassifier(props.get("testFile").toString());
    }
}
