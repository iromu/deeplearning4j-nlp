package com.iromu.dl.nlp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
@Builder
public class DatasetMeta {
    public Path trainPath;
    public Path testPath;
}
