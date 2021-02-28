package com.db.cloud.ai;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParseHtml {

    final String regex = "^[\\s\\S]*<head[^\\>]*>([\\s\\S]*)<\\/head>[\\s\\S]*$";
    final Pattern headPattern = Pattern.compile(regex, Pattern.MULTILINE);
    final String title = "^[\\s\\S]*(<title[^\\>]*>[\\s\\S]*<\\/title>)[\\s\\S]*$";
    final String meta = "(<meta.*?>)";
    final Pattern titlePattern = Pattern.compile(title, Pattern.MULTILINE);
    final Pattern metaPattern = Pattern.compile(meta, Pattern.MULTILINE);

    public static void main(String[] args) throws Exception {
        new ParseHtml().run();
    }

    private void run() throws IOException {
        List<Path> dataset = list(Paths.get("dataset"));

        for (Path path : dataset) {
            try {
//                log.info(path.toString());
                String html = Files.readString(path);
                if (html.replaceAll("\r", "").replaceAll("\n", "").trim().isEmpty()) continue;
                String content = "";
//                String head = extractHead(html);
//                if (head != null) {
//                    content += extractTitle(titlePattern, head);
//                    content += "\n" + String.join("\n", extractMeta(metaPattern, head));
//                }

                Document doc = Jsoup.parse(html);
                String title = doc.title();
                if (title != null) {
                    if (title.toLowerCase().contains("is for sale")) continue;
                    if (title.toLowerCase().contains("domain name")) continue;
                    content += title;
                }

                Elements metaTags = doc.getElementsByTag("meta");

                for (Element metaTag : metaTags) {
//                    String content = metaTag.attr("content");
//                    String name = metaTag.attr("name");
                    if (metaTag.hasAttr("charset")) continue;
                    if (metaTag.hasAttr("http-equiv")) continue;
                    if ("viewport".equalsIgnoreCase(metaTag.attr("name"))) continue;
                    String s = metaTag.toString();
                    log.warn(s);
                    content += "\n" + s;
                }

                Element body = doc.body();
                if (body != null) {
                    String text = body.text();
                    content += "\n" + text;
                }

                if (!content.isBlank()
                        && !content.toLowerCase().contains("domain name")
                        && !content.toLowerCase().contains("is for sale")
                        && !content.replaceAll("\r", "").replaceAll("\n", "").trim().isEmpty()
                ) {
                    Files.createDirectories(Paths.get("parsed", path.getParent().toString()));
                    Files.write(
                            Paths.get("parsed", path.toString()),
                            (content).getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    private String extractHead(String html) {
        final Matcher matcher = headPattern.matcher(html);
        boolean found = matcher.find();
        if (!found) return null;
        String head = matcher.group(1);
        return head;
    }

    private String extractTitle(Pattern titlePattern, String html) {
        final Matcher matcher = titlePattern.matcher(html);
        boolean found = matcher.find();
        if (!found) return "";
        String title = matcher.group(1).replaceAll("\r", "").replaceAll("\n", "");
        if ("<title></title>".equalsIgnoreCase(title)) return null;
        return title;
    }

    private List<String> extractMeta(Pattern titlePattern, String html) {
        List<String> items = new ArrayList<>();
        final Matcher matcher = titlePattern.matcher(html);
        while (matcher.find()) {
            items.add(matcher.group(1).replaceAll("\r", "").replaceAll("\n", ""));
        }
        return items;
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
}
