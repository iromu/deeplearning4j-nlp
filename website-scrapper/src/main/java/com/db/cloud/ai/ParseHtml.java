package com.db.cloud.ai;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.iromu.dl.nlp.DatasetPath.getRootPath;

@Slf4j
public class ParseHtml {
    public final Path datasetRootPath;
    private final Path datasetOriginalPath;
    private final Path datasetParsedPath;
    private final Path datasetMetaPath;

    public ParseHtml() {
        String path = getRootPath();
        datasetRootPath = Paths.get(path, "datasets", "URL Classification/");
        datasetOriginalPath = datasetRootPath.resolve("original");
        datasetParsedPath = datasetRootPath.resolve("parsed");
        datasetMetaPath = datasetRootPath.resolve("meta");
    }

    public static void main(String[] args) throws Exception {
        new ParseHtml().run();
    }

    public static List<Path> list(Path src) throws IOException {
        List<Path> fileList = new ArrayList<>();
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

    private boolean isNotEmptyOrInvalid(String content) {
        return !content.isBlank()
                && !content.toLowerCase().contains("domain name")
                && !content.toLowerCase().contains("is for sale")
                && !content.replaceAll("\r", "").replaceAll("\n", "").trim().isEmpty();
    }

    private void save(StringBuilder contentBuilder, Path parent, Path source) {
        try {
            String content = contentBuilder.toString();
            if (isNotEmptyOrInvalid(content)) {
                Files.createDirectories(parent.resolve(source.getParent().getFileName()));
                Files.write(
                        parent.resolve(source.getParent().getFileName()).resolve(source.getFileName()),
                        content.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void run() throws IOException {
        List<Path> dataset = list(datasetOriginalPath);

        try (ProgressBar pb = new ProgressBar("Parsing", dataset.size())) {
            log.info("Parsing");
            for (Path source : dataset) {
                try {
                    pb.step();
                    String html = Files.readString(source);
                    if (html.replaceAll("\r", "").replaceAll("\n", "").trim().isEmpty()) continue;
                    StringBuilder contentBuilder = new StringBuilder();
                    StringBuilder metaBuilder = new StringBuilder();

                    Document doc = Jsoup.parse(html.toLowerCase());

                    String title = doc.title();
                    if (title != null) {
                        if (title.toLowerCase().contains("is for sale")) continue;
                        if (title.toLowerCase().contains("domain name")) continue;
                        contentBuilder.append(title);
                        metaBuilder.append(title).append("\n");
                    }

                    Elements metaTags = doc.getElementsByTag("meta");

                    for (Element metaTag : metaTags) {
                        if (metaTag.hasAttr("charset")) continue;
                        if (metaTag.hasAttr("http-equiv")) continue;
                        if ("viewport".equalsIgnoreCase(metaTag.attr("name"))) continue;
                        String metaName = metaTag.attr("name");
                        if (metaName.toLowerCase().contains("description") ||
                                metaName.toLowerCase().contains("keywords") ||
                                metaName.toLowerCase().contains("title")) {
                            if (metaTag.hasAttr("content")) {
                                metaBuilder.append(metaTag.attr("content")).append("\n");
                                contentBuilder.append("\n").append(metaTag.attr("content"));
                            }
                        }
                    }
                    save(metaBuilder, datasetMetaPath, source);

                    Element body = doc.body();
                    if (body != null) {
                        String text = body.text();
                        contentBuilder.append("\n").append(text);
                    }
                    save(contentBuilder, datasetParsedPath, source);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
