package com.db.cloud.ai;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.db.cloud.ai.ParseHtml.list;
import static com.iromu.dl.nlp.DatasetPath.getRootPath;

@SpringBootApplication
@Slf4j
public class ScrapperApplication implements CommandLineRunner {
    public final Path datasetRootPath;
    private final Path datasetOriginalPath;
    private final Path datasetErrorsPath;
    private final Path datasetCsvPath;
    private final String path;
    private WebClient webClient;
    private final AtomicInteger errors = new AtomicInteger(0);
    private final AtomicInteger skipped = new AtomicInteger(0);


    public ScrapperApplication() {
        path = getRootPath();
        datasetRootPath = Paths.get(path, "datasets", "URL Classification/");
        datasetOriginalPath = datasetRootPath.resolve("original");
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
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(connector).build();
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("EXECUTING : command line runner. Path " + path);
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
        log.info("Errors file read. " + errorList.size());
        List<Path> dataset = list(datasetOriginalPath);
        log.info("Existing files read. " + dataset.size());
        String label = "";
        List<String> br = Files.readAllLines(datasetCsvPath);
        long lines = br.size();
        log.info("Total files to process. " + lines);
        try (ProgressBar pb = new ProgressBar("Processing", lines)) {
            //try (BufferedReader br = Files.newBufferedReader(datasetCsvPath)) {
            //String line;
            String index = "0";
            Scheduler scheduler = Schedulers.newBoundedElastic(5, 1000, "ScrapGroup");

            String category = "";
            for (String line : br) {
                //while ((line = br.readLine()) != null) {
                try {
                    pb.step();
                    String[] values = line.split(",");
                    index = values[0];
                    if (errorList.contains(index)) {
                        continue;
                    }
                    String url = values[1];
                    category = values[2];
                    if (!category.equals(label)) {
                        label = category;
                        pb.setExtraMessage(category);
                    }

                    Path outputFilePath = datasetOriginalPath.resolve(category).resolve(index + ".html");
                    if (!dataset.contains(outputFilePath)) {
                        //  if (!Files.exists(outputFilePath)) {
                        String finalIndex = index;

                        String finalCategory = category;
                        boolean parallel = false;
                        if (parallel) {
                            webClient.get()
                                    .uri(url)
                                    .accept(MediaType.TEXT_HTML)
                                    .exchangeToMono(clientResponse -> {
                                        if (clientResponse.statusCode().is2xxSuccessful()) {
                                            return clientResponse.bodyToMono(String.class);
                                        } else {
                                            return Mono.error(new Exception());
                                        }
                                    })
                                    .publishOn(scheduler)
                                    .timeout(Duration.ofSeconds(60))
                                    .doOnError(throwable -> error(finalIndex))
                                    .subscribe(s -> saveFile(outputFilePath, finalCategory, s), e -> error(finalIndex));
                        } else {
                            String body = webClient.get()
                                    .uri(url)
                                    .accept(MediaType.TEXT_HTML)
                                    .exchangeToMono(clientResponse -> {
                                        if (clientResponse.statusCode().is2xxSuccessful()) {
                                            return clientResponse.bodyToMono(String.class);
                                        } else {
                                            return Mono.error(new Exception());
                                        }
                                    })
                                    .publishOn(scheduler)
                                    .timeout(Duration.ofSeconds(60))
                                    .doOnError(throwable -> error(finalIndex))
                                    .block(Duration.ofSeconds(60));
                            saveFile(outputFilePath, category, body);
                        }

                    } else {

                    }
                } catch (Throwable e) {
                    error(index);
                }
            }
        }
        //  }

    }

    private void error(String index) {
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

    private void saveFile(Path outputFilePath, String category, String s) {
        try {
            Files.createDirectories(datasetOriginalPath.resolve(category));

            Files.write(
                    outputFilePath,
                    s.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            // System.out.print("*");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
