package com.scb.paymentintegration.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@ExtendWith({MockitoExtension.class})
class FileUtilsTest {
    private static final String TEST_DIR = "opt/TEST/";

    @Test
    void shouldCleanDirectory() throws IOException {
        try {
            File file = new File(TEST_DIR.concat("temp.txt"));
            file.createNewFile();
        } finally {
            File directory = new File(TEST_DIR);
            FileUtils.cleanDirectory(TEST_DIR);
            if(Objects.nonNull(directory)) {
                Assertions.assertEquals(0, directory.listFiles().length);
            }
        }
    }
}
