package com.scb.paymentintegration.controller;

import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.service.FileHandlerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileHandlerControllerTest {
    @InjectMocks
    private FileHandlerController fileHandlerController;

    @Mock
    private FileHandlerService fileHandlerService;

    private static final String S3_URL = "temp";

    @Test
    void shouldThrowExceptionGenerateAndUploadS1File() throws IOException {
        when(fileHandlerService.generateAndUploadS1File(any(RiderSettlementRequest.class))).thenThrow(new NullPointerException());
        RiderSettlementRequest request = RiderSettlementRequest.builder().build();
        assertThrows(NullPointerException.class, () -> fileHandlerController.generateAndUploadS1File(request));
    }

    @Test
    void shouldGenerateAndUploadS1File() throws IOException {
        when(fileHandlerService.generateAndUploadS1File(any(RiderSettlementRequest.class))).thenReturn(S3_URL);
        RiderSettlementRequest request = RiderSettlementRequest.builder().build();
        ResponseEntity<String> result = fileHandlerController.generateAndUploadS1File(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(S3_URL, result.getBody());
    }
}
