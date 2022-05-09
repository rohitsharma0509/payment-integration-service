package com.scb.paymentintegration.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.scb.paymentintegration.client.SettlementFeignClient;
import com.scb.paymentintegration.client.SftpClient;
import com.scb.paymentintegration.client.handler.OperationServiceClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.dto.ReturnFileResult;
import com.scb.paymentintegration.dto.SftpRequest;
import com.scb.paymentintegration.service.CryptoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpServiceImplTest {

    private static final String RET_FILE_NAME = "RBH0001_20210127_DCP_200.txt.gpg";
    private static final String CTRL_FILE_NAME = "RBH0001_20210127_DCP_200.ctrl";
    private static final String BUCKET = "payment/";
    private static final String PRIVATE_KEY_PATH = "pri-key.asc";
    private static final String PASSPHRASE = "test";
    private static final String INPUT_PATH = "payment_input/";
    private static final String OUTPUT_PATH = "payment_output/";
    private static final String BATCH_REF_NO = "S00001";
    private static final String COMPANY_ID = "RBH00001";
    private static final String TEST_FILES_PATH = "test-files/";
    private static final String ROOT_DIR = "/NASHOME/rbhusr/";
    private static final String RETURN_FILE_PATH = TEST_FILES_PATH.concat(RET_FILE_NAME);
    private static final int EXPECTED_1_TIME = 1;
    private static final int EXPECTED_2_TIME = 2;

    @InjectMocks
    private SftpServiceImpl sftpServiceImpl;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Mock
    private OperationServiceClient operationServiceClient;

    @Mock
    private SettlementFeignClient settlementFeignClient;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private SftpClient sftpClient;

    @Mock
    private ChannelSftp channelSftp;

    @Mock
    private ChannelSftp.LsEntry lsEntry;

    @Mock
    private InputStream is;

    @Test
    void shouldUploadFile() throws IOException, SftpException {
        ReflectionTestUtils.setField(sftpServiceImpl, "s1FileBucketName", BUCKET);

        BatchConfigurationDto config = BatchConfigurationDto.builder().s1InputFolderFilePath(INPUT_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        when(amazonS3Service.downloadFile(anyString(), eq(BUCKET), eq(BATCH_REF_NO))).thenReturn(new byte[100]);
        when(sftpClient.connect()).thenReturn(channelSftp);
        SftpRequest request = SftpRequest.builder().fileName(RET_FILE_NAME)
                .batchReferenceNumber(BATCH_REF_NO).build();
        boolean result = sftpServiceImpl.uploadFile(request);
        assertTrue(result);
        verify(channelSftp, times(EXPECTED_2_TIME)).put(any(InputStream.class), anyString());
        verify(sftpClient, times(EXPECTED_1_TIME)).disconnect(any(ChannelSftp.class));
    }

    @Test
    void shouldNotUploadFile() throws IOException, SftpException {
        ReflectionTestUtils.setField(sftpServiceImpl, "s1FileBucketName", BUCKET);

        BatchConfigurationDto config = BatchConfigurationDto.builder().build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        when(amazonS3Service.downloadFile(anyString(), eq(BUCKET), eq(BATCH_REF_NO))).thenReturn(new byte[100]);
        when(sftpClient.connect()).thenReturn(channelSftp);
        doThrow(new SftpException(1, "failed to put file")).when(channelSftp).put(any(InputStream.class), anyString());
        SftpRequest request = SftpRequest.builder().fileName(RET_FILE_NAME)
                .sftpDestinationDir(INPUT_PATH).batchReferenceNumber(BATCH_REF_NO).build();
        boolean result = sftpServiceImpl.uploadFile(request);
        assertFalse(result);
        verify(sftpClient, times(EXPECTED_1_TIME)).disconnect(any(ChannelSftp.class));
    }

    @Test
    void pollOutputPathWhileExceptionOccurs() throws SftpException {
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).s1OutputFolderFilePath(OUTPUT_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);

        when(sftpClient.connect()).thenReturn(channelSftp);
        when(channelSftp.ls(COMPANY_ID.concat("*.ctrl"))).thenThrow(new SftpException(1, "failed to change dir"));
        boolean result = sftpServiceImpl.pollOutputPath();
        assertFalse(result);
        verify(channelSftp, times(EXPECTED_1_TIME)).cd(OUTPUT_PATH);
        verify(sftpClient, times(EXPECTED_1_TIME)).disconnect(any(ChannelSftp.class));
    }

    @Test
    void pollOutputPathWhenNoCtrlFileOnOutputPath() throws SftpException {
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).s1OutputFolderFilePath(OUTPUT_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);

        when(sftpClient.connect()).thenReturn(channelSftp);
        when(channelSftp.ls(COMPANY_ID.concat("*.ctrl"))).thenReturn(new Vector());
        boolean result = sftpServiceImpl.pollOutputPath();
        assertFalse(result);
        verify(channelSftp, times(EXPECTED_1_TIME)).cd(OUTPUT_PATH);
        verify(sftpClient, times(EXPECTED_1_TIME)).disconnect(any(ChannelSftp.class));
    }

    @Test
    void pollOutputPathWhenSettlementServiceNotAbleToSave() throws SftpException, IOException {
        ReflectionTestUtils.setField(sftpServiceImpl, "privateKeyPath", PRIVATE_KEY_PATH);
        ReflectionTestUtils.setField(sftpServiceImpl, "passphrase", PASSPHRASE);
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).s1OutputFolderFilePath(OUTPUT_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);

        when(sftpClient.connect()).thenReturn(channelSftp);
        when(channelSftp.pwd()).thenReturn(ROOT_DIR);
        Vector<ChannelSftp.LsEntry> list = new Vector();
        list.add(lsEntry);
        when(channelSftp.ls(COMPANY_ID.concat("*.ctrl"))).thenReturn(list);
        when(lsEntry.getFilename()).thenReturn(CTRL_FILE_NAME);
        when(channelSftp.get(eq(RET_FILE_NAME))).thenReturn(is);

        try (
            InputStream decryptedReturnFileStream = getClass().getClassLoader().getResourceAsStream(RETURN_FILE_PATH);
        ) {
            when(cryptoService.decrypt(any(InputStream.class), eq(PRIVATE_KEY_PATH), any())).thenReturn(decryptedReturnFileStream);
            when(settlementFeignClient.saveReturnFileResponse(any(ReturnFileResult.class))).thenThrow(new NullPointerException());
            boolean result = sftpServiceImpl.pollOutputPath();
            assertTrue(result);
            verify(channelSftp, times(EXPECTED_2_TIME)).cd(OUTPUT_PATH);
            verify(sftpClient, times(2)).disconnect(any(ChannelSftp.class));
        }
    }

    @Test
    void pollOutputPath() throws SftpException, IOException {
        ReflectionTestUtils.setField(sftpServiceImpl, "privateKeyPath", PRIVATE_KEY_PATH);
        ReflectionTestUtils.setField(sftpServiceImpl, "passphrase", PASSPHRASE);
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).s1OutputFolderFilePath(OUTPUT_PATH).build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);

        when(sftpClient.connect()).thenReturn(channelSftp);
        when(channelSftp.pwd()).thenReturn(ROOT_DIR);
        Vector<ChannelSftp.LsEntry> list = new Vector();
        list.add(lsEntry);
        when(channelSftp.ls(COMPANY_ID.concat("*.ctrl"))).thenReturn(list);
        when(lsEntry.getFilename()).thenReturn(CTRL_FILE_NAME);
        when(channelSftp.get(eq(RET_FILE_NAME))).thenReturn(is);
        try (
                InputStream decryptedReturnFileStream = getClass().getClassLoader().getResourceAsStream(RETURN_FILE_PATH);
        ) {
            when(cryptoService.decrypt(any(InputStream.class), eq(PRIVATE_KEY_PATH), any())).thenReturn(decryptedReturnFileStream);
            when(settlementFeignClient.saveReturnFileResponse(any(ReturnFileResult.class))).thenReturn(Boolean.TRUE);
            boolean result = sftpServiceImpl.pollOutputPath();
            assertTrue(result);
            verify(channelSftp, times(3)).cd(OUTPUT_PATH);
            verify(channelSftp, times(EXPECTED_2_TIME)).rename(anyString(), anyString());
            verify(sftpClient, times(3)).disconnect(any(ChannelSftp.class));
        }
    }

}
