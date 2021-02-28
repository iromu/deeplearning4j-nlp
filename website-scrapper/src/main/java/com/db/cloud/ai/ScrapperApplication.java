package com.db.cloud.ai;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class ScrapperApplication implements CommandLineRunner {


    // /mnt/wd3/datasets/URL Classification/
    public final Path datasetRootPath;
    private final Path datasetOriginalPath;
    private final Path datasetErrorsPath;
    private final Path datasetParsedPath;
    private final Path datasetCsvPath;
    private WebClient webClient;

    public ScrapperApplication() {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank())
            path = System.getProperty("user.home");
        datasetRootPath = Paths.get(path, "datasets", "URL Classification/");
        datasetOriginalPath = datasetRootPath.resolve("original");
        datasetParsedPath = datasetRootPath.resolve("parsed");
        datasetErrorsPath = datasetRootPath.resolve("errors.txt");
        datasetCsvPath = datasetRootPath.resolve("URL Classification.csv");
    }

    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(ScrapperApplication.class, args);
        log.info("APPLICATION FINISHED");
    }

    public WebClient createWebClient() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext)).followRedirect(true);
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("EXECUTING : command line runner");
        Files.createDirectories(datasetOriginalPath);
        this.webClient = createWebClient();
        List<String> errorList = new ArrayList<>();
        if (Files.exists(datasetErrorsPath)) {
            String errors = Files.readString(datasetErrorsPath);
            errorList = Arrays.stream(errors.split("\n")).distinct().collect(Collectors.toList());
            try {
                Files.write(
                        datasetErrorsPath,
                        String.join("\n", errorList).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        String label = "";

        try (BufferedReader br = Files.newBufferedReader(datasetCsvPath)) {
            String line;
            String index = "0";
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(",");
                    index = values[0];
                    if (errorList.contains(index)) continue;
                    String url = values[1];
                    String category = values[2];
                    if (!category.equals(label)) {
                        label = category;
                        log.info(category);
                    }

                    Path outputFilePath = datasetOriginalPath.resolve(category).resolve(index + ".html");
                    if (!Files.exists(outputFilePath)) {
                        String finalIndex = index;
                        ResponseEntity<String> s = webClient.get()
                                .uri(url)
                                .accept(MediaType.TEXT_HTML)
                                .exchange()
                                .filter(clientResponse -> clientResponse.statusCode().is2xxSuccessful())
                                .flatMap(response -> response.toEntity(String.class))
                                .doOnError(throwable -> {
                                    error(finalIndex);
                                }).block(Duration.ofSeconds(10));
                        saveFile(outputFilePath, category, s);
                    }
                } catch (Exception e) {
                    error(index);
                }
            }
        }
    }

    private void error(String index) {
        System.out.print(".");
        try {
            Files.write(
                    datasetErrorsPath,
                    (index + "\n").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void saveFile(Path outputFilePath, String category, ResponseEntity<String> s) {
        try {
            Files.createDirectories(datasetOriginalPath.resolve(category));

            Files.write(
                    outputFilePath,
                    s.getBody().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.print("*");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
