package com.iromu.dl4j.nlp.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Slf4j
public class DatasetUtil {


    public final Path workingPath;
    public final Path trainPath;
    public final Path testPath;
    public static final Path datasetPath = Paths.get(System.getProperty("user.home"), "datasets", "20news-18828");
    private final DatasetNormalizer normalizer;

    public static void main(String[] args) throws IOException {
        DatasetUtil datasetUtil = new DatasetUtil();
        DatasetMeta datasetMeta = datasetUtil.prepareDataset();

        //Reuse the random train/test split for above command
        Path testPath = datasetMeta.testPath;
        Path trainPath = datasetMeta.trainPath;

        // Standford ColumnDataClassifier single tabular file format
        datasetUtil.columndDataFormat(datasetUtil.list(testPath), datasetUtil.workingPath.resolve("stanford-classifier.test"));
        datasetUtil.columndDataFormat(datasetUtil.list(trainPath), datasetUtil.workingPath.resolve("stanford-classifier.train"));

        //OpenNlp POS lemma normalization
        datasetUtil = new DatasetUtil(new OpenNlpDatasetNormalizer());
        if (!datasetUtil.trainPath.toFile().exists()) {
            datasetUtil.copyFiles(datasetUtil.list(testPath), datasetUtil.testPath);
            datasetUtil.copyFiles(datasetUtil.list(trainPath), datasetUtil.trainPath);
        }

        // Standford ColumnDataClassifier single tabular file format
        datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.testPath), datasetUtil.workingPath.resolve("stanford-classifier.test"));
        datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.trainPath), datasetUtil.workingPath.resolve("stanford-classifier.train"));

        //Standford pipeline POS lemma ner normalization
        datasetUtil = new DatasetUtil(new CoreNlpDatasetNormalizer());
        if (!datasetUtil.trainPath.toFile().exists()) {
            datasetUtil.copyFiles(datasetUtil.list(testPath), datasetUtil.testPath);
            datasetUtil.copyFiles(datasetUtil.list(trainPath), datasetUtil.trainPath);
//
//            // Standford ColumnDataClassifier single tabular file format
//            datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.testPath), datasetUtil.workingPath.resolve("stanford-classifier.test"));
//            datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.trainPath), datasetUtil.workingPath.resolve("stanford-classifier.train"));
        }

        // Standford ColumnDataClassifier single tabular file format
        datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.testPath), datasetUtil.workingPath.resolve("stanford-classifier.test"));
        datasetUtil.columndDataFormat(datasetUtil.list(datasetUtil.trainPath), datasetUtil.workingPath.resolve("stanford-classifier.train"));
    }

    private void columndDataFormat(List<Path> paths, Path destination) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Formatting {} files to destination path {}", paths.size(), destination);
        BufferedWriter writer = new BufferedWriter(new FileWriter(destination.toFile()));
        for (Path path : paths) {
            Path parent = path.getParent().getFileName();
            Path fileName = path.getFileName();

            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append(" ");

            reader.close();

            String content = builder.toString().replaceAll("\\s+", " ");
            String process = parent + "\t" + fileName + "\t" + content + "\n";
            writer.write(process);
        }
        writer.close();
        log.info("Formatted {} files to destination path {} in {}ms", paths.size(), destination, System.currentTimeMillis() - start);
    }

    public DatasetUtil() {
        this(null);
    }

    public DatasetUtil(DatasetNormalizer normalizer) {
        this.normalizer = normalizer;
        String simpleName = (normalizer != null) ? normalizer.getClass().getSimpleName() : "nonormalizer";

        workingPath = Paths.get(System.getProperty("user.home"), "datasets", "20news-18828-workdir", simpleName);
        workingPath.toFile().mkdirs();
        trainPath = workingPath.resolve("train");
        testPath = workingPath.resolve("test");
        try {
            prepareDataset();
        } catch (IOException e) {
            new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class DatasetMeta {
        public Path trainPath;
        public Path testPath;
    }

    public DatasetMeta prepareDataset() throws IOException {
        log.info("Preparing dataset at destination path {}", workingPath);
        long start = System.currentTimeMillis();
        if (!trainPath.toFile().exists()) {

            // Create and reset folders
            createAndResetFolder(trainPath);
            createAndResetFolder(testPath);

            // Get random training and test files
            List<Path> fileList = list(datasetPath);

            // Group by Label before shuffling
            Map<Path, List<Path>> map = new HashMap<>();
            for (Path path : fileList) {
                Path category = path.getParent().getFileName();
                if (map.containsKey(category)) {
                    map.get(category).add(path);
                } else {
                    ArrayList<Path> paths = new ArrayList<>();
                    paths.add(path);
                    map.put(category, paths);
                }
            }

            for (Path category : map.keySet()) {
                List<Path> paths = map.get(category);

                Collections.shuffle(paths);
                int total = paths.size();
                int trainSize = (int) Math.round(total * .8);

                List<Path> trainList = paths.subList(0, trainSize);
                List<Path> testList = paths.subList(trainSize, total);

                log.info("Preparing train and test subsets for category {}", category);
                copyFiles(trainList, trainPath);
                copyFiles(testList, testPath);
            }
        }
        log.info("Dataset prepared in {}ms", System.currentTimeMillis() - start);
        return DatasetMeta.builder().testPath(testPath).trainPath(trainPath).build();
    }

    private void copyFiles(List<Path> paths, Path destination) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Copying {} files to destination path {}", paths.size(), destination);
        for (Path path : paths) {
            Path category = path.getParent().getFileName();
            Path fileName = path.getFileName();
            Path destinationFolder = destination.resolve(category);
            destinationFolder.toFile().mkdirs();
            Files.copy(path, destinationFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            // normalize destinationp
            normalize(destinationFolder.resolve(fileName));
        }
        log.info("Copied {} files to destination path {} in {}ms", paths.size(), destination, System.currentTimeMillis() - start);
    }

    private void normalize(Path path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line).append(" ");

            reader.close();

            String content = builder.toString().replaceAll("\\s+", " ");
            if (normalizer != null)
                content = normalizer.process(content);

            BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Path> list(Path src) throws IOException {
        List<Path> fileList = new ArrayList<>();

        // Read original dataset file list
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(path);
                } else {
                    fileList.addAll(list(path));
                }
            }
        }
        return fileList;
    }

    private void createAndResetFolder(Path path) throws IOException {
        if (Files.exists(path))
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        path.toFile().mkdirs();
    }

}
