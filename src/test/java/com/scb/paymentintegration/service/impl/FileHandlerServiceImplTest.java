package com.scb.paymentintegration.service.impl;

import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.enums.FileType;
import com.scb.paymentintegration.service.S1FileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileHandlerServiceImplTest {

    @InjectMocks
    private FileHandlerServiceImpl fileHandlerService;

    @Mock
    private S1FileGenerator s1FileGenerator;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Mock
    private CryptoServiceImpl cryptoService;

    @Mock
    private File s1File;

    @Mock
    File s1FileWithHash = null;

    @Mock
    File ctrlFile = null;

    @Mock
    File encryptedFile = null;

    private static final String BATCH_REF_NO = "S00001";
    private static final String S1_FILE_NAME = "BulkERP_RBH0001014PAY20210122143959.txt";
    private static final String S1_FILE_WITH_HASH = "hash_".concat(S1_FILE_NAME);
    private static final String CTRL_FILE_NAME = "BulkERP_RBH0001014PAY20210122143959.ctrl";
    private static final String ENCRYPTED_S1_FILE_NAME = S1_FILE_NAME.concat(FileType.GPG.getExtension());
    private static final String OUTPUT_PATH = "opt/TEST/";
    private static final String BUCKET = "TEST";
    private static final String PUBLIC_KEY = "pub.gpg";

    @Test
    void generateAndUploadS1File() throws IOException {
        ReflectionTestUtils.setField(fileHandlerService, "publicKeyPath", PUBLIC_KEY);
        ReflectionTestUtils.setField(fileHandlerService, "s1FileBucketName", BUCKET);
        ReflectionTestUtils.setField(fileHandlerService, "s1OutputPath", OUTPUT_PATH);
        RiderSettlementRequest request = RiderSettlementRequest.builder().batchReferenceNumber(BATCH_REF_NO).build();
        when(s1FileGenerator.generateS1File(any(RiderSettlementRequest.class))).thenReturn(s1File);
        when(s1File.getName()).thenReturn(S1_FILE_NAME);
        when(s1FileGenerator.generateS1FileWithHash(S1_FILE_NAME)).thenReturn(s1FileWithHash);
        when(s1FileGenerator.generateCtrlFile(ENCRYPTED_S1_FILE_NAME)).thenReturn(ctrlFile);
        when(cryptoService.encrypt(any(File.class), eq(PUBLIC_KEY), eq(ENCRYPTED_S1_FILE_NAME), eq(Boolean.FALSE))).thenReturn(encryptedFile);
        when(ctrlFile.getName()).thenReturn(CTRL_FILE_NAME);
        when(encryptedFile.getName()).thenReturn(ENCRYPTED_S1_FILE_NAME);
        when(s1File.toPath()).thenReturn(Paths.get(S1_FILE_NAME));
        when(s1FileWithHash.toPath()).thenReturn(Paths.get(S1_FILE_WITH_HASH));
        when(ctrlFile.toPath()).thenReturn(Paths.get(CTRL_FILE_NAME));
        when(encryptedFile.toPath()).thenReturn(Paths.get(ENCRYPTED_S1_FILE_NAME));
        when(amazonS3Service.uploadFile(anyString(), any(File.class), any(), eq(BATCH_REF_NO))).thenReturn(CTRL_FILE_NAME, ENCRYPTED_S1_FILE_NAME);
        String result = fileHandlerService.generateAndUploadS1File(request);
        assertEquals(ENCRYPTED_S1_FILE_NAME, result);
    }
}
