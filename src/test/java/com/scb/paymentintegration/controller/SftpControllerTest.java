package com.scb.paymentintegration.controller;

import com.scb.paymentintegration.dto.SftpRequest;
import com.scb.paymentintegration.service.SftpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpControllerTest {
    @InjectMocks
    private SftpController sftpController;

    @Mock
    private SftpService sftpService;

    private static final String FILE_NAME = "temp.txt";

    @Test
    void throwExceptionUploadFileToSftp() throws IOException {
        SftpRequest request = SftpRequest.builder().build();
        when(sftpService.uploadFile(any(SftpRequest.class))).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class, () -> sftpController.uploadFileToSftp(request));
    }

    @Test
    void shouldUploadFileToSftp() throws IOException {
        SftpRequest request = SftpRequest.builder().build();
        when(sftpService.uploadFile(any(SftpRequest.class))).thenReturn(Boolean.TRUE);
        ResponseEntity<Boolean> result = sftpController.uploadFileToSftp(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }

    @Test
    void throwExceptionPollSftp() {
        when(sftpService.pollOutputPath()).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class, () -> sftpController.pollSftp());
    }

    @Test
    void shouldPollSftp() {
        when(sftpService.pollOutputPath()).thenReturn(Boolean.TRUE);
        ResponseEntity<Boolean> result = sftpController.pollSftp();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }
}
