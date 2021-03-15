package com.scb.paymentintegration.service.impl;

import com.scb.paymentintegration.client.handler.OperationServiceClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.dto.RiderSettlementDetails;
import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.exception.HashProgramValidationException;
import com.scb.paymentintegration.exception.InvalidDataException;
import com.scb.paymentintegration.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TxtS1FileGeneratorTest {

    @InjectMocks
    private TxtS1FileGenerator txtS1FileGenerator;

    @Mock
    private OperationServiceClient operationServiceClient;

    private static final String s1OutputPath = "opt/TEST/";

    private final RiderSettlementRequest riderSettlementRequest = RiderSettlementRequest.builder()
            .batchReferenceNumber("TEMP001").noOfCredits(1)
            .totalDebitAmount(1000.0).totalConsolidatedAmount(1000.0).build();

    @Test
    void throwExceptionGenerateS1File() {
        BatchConfigurationDto config = BatchConfigurationDto.builder().build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        assertThrows(InvalidDataException.class, () -> txtS1FileGenerator.generateS1File(null));
    }

    @Test
    void shouldGenerateS1File() throws IOException {
        File s1File = null;
        try {
            RiderSettlementDetails riderSettlementDetails = RiderSettlementDetails.builder()
                    .riderId("RR0001").riderName("Temp Rider").riderAccountNumber("1115168927").totalCreditAmount(1000.0)
                    .build();
            riderSettlementRequest.setRiderSettlementDetails(Arrays.asList(riderSettlementDetails));
            s1File = getS1File(riderSettlementRequest);
        } finally {
            if (Objects.nonNull(s1File)) Files.deleteIfExists(s1File.toPath());
        }
    }

    @Test
    void throwExceptionGenerateS1FileWithHashWhenRiderAccountNotValid() throws IOException {
        File s1File = null;
        try {
            RiderSettlementDetails riderSettlementDetails = RiderSettlementDetails.builder()
                    .riderId("RR0001").riderName("Temp Rider").riderAccountNumber("1115-168-927").totalCreditAmount(1000.0)
                    .build();
            riderSettlementRequest.setRiderSettlementDetails(Arrays.asList(riderSettlementDetails));
            s1File = getS1File(riderSettlementRequest);
            ReflectionTestUtils.setField(txtS1FileGenerator, "s1OutputPath", s1OutputPath);
            final String s1FileName = s1File.getName();
            assertThrows(InvalidDataException.class, () -> txtS1FileGenerator.generateS1FileWithHash(s1FileName));
        } finally {
            if (Objects.nonNull(s1File)) Files.deleteIfExists(s1File.toPath());
            FileUtils.cleanDirectory(s1OutputPath);
        }
    }

    @Test
    void throwExceptionGenerateS1FileWithHashWhenS1FileNotExist() {
        try {
            ReflectionTestUtils.setField(txtS1FileGenerator, "s1OutputPath", s1OutputPath);
            final String s1FileName = "temp.txt";
            assertThrows(HashProgramValidationException.class, () -> txtS1FileGenerator.generateS1FileWithHash(s1FileName));
        } finally {
            FileUtils.cleanDirectory(s1OutputPath);
        }
    }

    @Test
    void shouldGenerateS1FileWithHash() throws IOException {
        File s1File = null;
        File s1FileWithHash = null;
        try {
            RiderSettlementDetails riderSettlementDetails = RiderSettlementDetails.builder()
                    .riderId("RR0001").riderName("Temp Rider").riderAccountNumber("1115168927").totalCreditAmount(1000.0)
                    .build();
            riderSettlementRequest.setRiderSettlementDetails(Arrays.asList(riderSettlementDetails));
            s1File = getS1File(riderSettlementRequest);
            ReflectionTestUtils.setField(txtS1FileGenerator, "s1OutputPath", s1OutputPath);
            s1FileWithHash = txtS1FileGenerator.generateS1FileWithHash(s1File.getName());
        } finally {
            if (Objects.nonNull(s1File)) Files.deleteIfExists(s1File.toPath());
            if (Objects.nonNull(s1FileWithHash)) Files.deleteIfExists(s1FileWithHash.toPath());
            FileUtils.cleanDirectory(s1OutputPath);
        }
    }

    @Test
    void shouldGenerateCtrlFile() throws IOException {
        File ctrlFile = null;
        try {
            String s1FileName = "BulkERP_RBH0001014PAY20210122143959.txt.gpg";
            ctrlFile = txtS1FileGenerator.generateCtrlFile(s1FileName);
            assertNotNull(ctrlFile);
        } finally {
            if (Objects.nonNull(ctrlFile)) Files.deleteIfExists(ctrlFile.toPath());
        }
    }

    private File getS1File(RiderSettlementRequest request) throws IOException {
        BatchConfigurationDto config = BatchConfigurationDto.builder()
                .companyId("CMP001")
                .rhDebitAccNum("1113942888")
                .build();
        when(operationServiceClient.getBatchConfiguration()).thenReturn(config);
        File s1File = txtS1FileGenerator.generateS1File(request);
        assertNotNull(s1File);
        return s1File;
    }
}
