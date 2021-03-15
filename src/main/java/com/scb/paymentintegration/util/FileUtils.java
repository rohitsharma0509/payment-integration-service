package com.scb.paymentintegration.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class FileUtils {
    private FileUtils() {}

    public static void cleanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if(null != directory.listFiles()) {
            for (File file : directory.listFiles())
                try {
                    if (!Files.deleteIfExists(file.toPath())) {
                        log.error("Error while deleting file {}", file.getName());
                    }
                } catch(IOException e) {
                    log.error("Exception occurred while deleting file {} from directory {}. ", file.getName(), directory.getName());
                }
        }
    }
}
