package com.iromu.dl.nlp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatasetPath {
    public static String getRootPath() {
        String path = System.getenv("DATASET_PATH");
        if (path == null || path.isBlank())
            path = System.getProperty("user.home");

        log.info("Root Path " + path);
        return path;
    }
}
