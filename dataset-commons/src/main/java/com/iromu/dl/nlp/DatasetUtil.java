package com.iromu.dl.nlp;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static com.iromu.dl.nlp.DatasetPath.getRootPath;

@Slf4j
public class DatasetUtil {


    public final Path workingPath;
    public final Path trainPath;
    public final Path testPath;
    public final Path datasetPath;
    private final DatasetNormalizer normalizer;
    public DatasetMeta datasetMeta;

    public DatasetUtil(DatasetNormalizer normalizer, String origin, String workdir) {
        this(normalizer,
                Paths.get(getRootPath(), "datasets", origin),
                Paths.get(getRootPath(), "datasets", workdir));
    }

    public DatasetUtil(DatasetNormalizer normalizer, Path origin, Path workdir) {
        this.normalizer = normalizer;
        String simpleName = normalizer.getClass().getSimpleName();

        datasetPath = origin;
        workingPath = workdir.resolve(simpleName);
        workingPath.toFile().mkdirs();
        trainPath = workingPath.resolve("train");
        testPath = workingPath.resolve("test");

        try {
            datasetMeta = prepareDataset();
        } catch (IOException e) {
            new RuntimeException(e);
        }
    }

    public void columndDataFormat(List<Path> paths, Path destination) throws IOException {
        long start = System.currentTimeMillis();
//        log.info("Formatting {} files to destination path {}", paths.size(), destination);
        if (!Files.exists(destination))
            try (ProgressBar pb = new ProgressBar(destination.getFileName().toString(), paths.size())) {

                BufferedWriter writer = new BufferedWriter(new FileWriter(destination.toFile()));
                for (Path path : paths) {
                    pb.step();
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
//            log.info("Formatted {} files to destination path {} in {}ms", paths.size(), destination, System.currentTimeMillis() - start);
            }
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

                // log.info("Preparing train and test subsets for category {}", category);
                copyFiles(category, trainList, trainPath);
                copyFiles(category, testList, testPath);
            }
        }
        log.info("Dataset prepared in {}ms", System.currentTimeMillis() - start);
        return DatasetMeta.builder().testPath(testPath).trainPath(trainPath).build();
    }

    public void copyFiles(Path root, List<Path> paths, Path destination) throws IOException {
        long start = System.currentTimeMillis();
        // log.info("Copying {} files to destination path {}", paths.size(), destination);
        try (ProgressBar pb = new ProgressBar(root.getFileName().toString() + " " + destination.getFileName().toString(), paths.size())) {
            for (Path path : paths) {
                pb.step();
                Path category = path.getParent().getFileName();
                Path fileName = path.getFileName();
                Path destinationFolder = destination.resolve(category);
                destinationFolder.toFile().mkdirs();
                Files.copy(path, destinationFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                // normalize destinationp
                normalize(destinationFolder.resolve(fileName));
            }
        }
        //log.info("Copied {} files to destination path {} in {}ms", paths.size(), destination, System.currentTimeMillis() - start);
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

    public List<Path> list(Path src) throws IOException {
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

    public void stanford() throws IOException {
        // Standford ColumnDataClassifier single tabular file format
        this.columndDataFormat(this.list(testPath), workingPath.resolve("stanford-classifier.test"));
        this.columndDataFormat(this.list(trainPath), workingPath.resolve("stanford-classifier.train"));

//        //Standford pipeline POS lemma ner normalization
//        if (!this.trainPath.toFile().exists()) {
//            this.copyFiles(this.list(trainPath), this.trainPath);
//        }
//        if (!this.testPath.toFile().exists()) {
//            this.copyFiles(this.list(testPath), this.testPath);
//        }

    }
}
