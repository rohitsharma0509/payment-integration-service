package com.scb.paymentintegration.service.impl;

import com.scb.paymentintegration.client.handler.OperationServiceClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.dto.RiderSettlementDetails;
import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.dto.S1CreditEntry;
import com.scb.paymentintegration.dto.S1DebitEntry;
import com.scb.paymentintegration.dto.S1HeaderEntry;
import com.scb.paymentintegration.dto.S1PayeeEntry;
import com.scb.paymentintegration.dto.S1TrailerEntry;
import com.scb.paymentintegration.enums.FileType;
import com.scb.paymentintegration.exception.HashProgramValidationException;
import com.scb.paymentintegration.exception.InvalidDataException;
import com.scb.paymentintegration.service.S1FileGenerator;
import com.scb.paymentintegration.util.CommonUtils;
import com.scb.paymentintegration.util.S1FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Service
public class TxtS1FileGenerator implements S1FileGenerator {

    private static final String NEW_LINE = "\r\n";

    @Value("${s1.hashing.output-path}")
    private String s1OutputPath;

    @Autowired
    private OperationServiceClient operationServiceClient;

    @Override
    public File generateS1File(RiderSettlementRequest riderSettlementRequest) throws IOException {
        log.debug("s1 file generation starts");
        BatchConfigurationDto batchConfig = operationServiceClient.getBatchConfiguration();
        String companyId = batchConfig.getCompanyId();
        String rhDebitAccountNumber = batchConfig.getRhDebitAccNum();

        if (Objects.nonNull(riderSettlementRequest) && !CollectionUtils.isEmpty(riderSettlementRequest.getRiderSettlementDetails())) {
            String fileName = S1FileUtils.getS1FileName(companyId, FileType.TXT.getExtension());
            File s1File = new File(fileName);
            try(
                OutputStream fos = new FileOutputStream(s1File);
                Writer fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            ) {
                fw.append(S1HeaderEntry.of(companyId, riderSettlementRequest.getBatchReferenceNumber()).toS1FormattedString()).append(NEW_LINE);
                fw.append(S1DebitEntry.of(rhDebitAccountNumber, riderSettlementRequest.getTotalDebitAmount(), riderSettlementRequest.getNoOfCredits()).toS1FormattedString()).append(NEW_LINE);
                int seq = 0;
                for (RiderSettlementDetails riderSettlementDetails : riderSettlementRequest.getRiderSettlementDetails()) {
                    seq++;
                    fw.append(S1CreditEntry.of(riderSettlementDetails, seq).toS1FormattedString()).append(NEW_LINE);
                    fw.append(S1PayeeEntry.of(seq).toS1FormattedString()).append(NEW_LINE);
                }
                fw.append(S1TrailerEntry.of(riderSettlementRequest.getTotalConsolidatedAmount(), riderSettlementRequest.getRiderSettlementDetails().size()).toS1FormattedString()).append(NEW_LINE);
                fw.flush();
            }
            log.debug("s1 file generated with name {}", fileName);
            return s1File;
        } else {
            throw new InvalidDataException("Invalid request, no data available in request");
        }
    }

    @Override
    public File generateS1FileWithHash(String s1TxtFilePath) throws IOException {
        log.debug("s1 file hash generation starts");
        if (Files.notExists(Paths.get(s1OutputPath))) {
            Files.createDirectories(Paths.get(s1OutputPath));
        }

        log.debug("s1 file hash validation program starts");
        String[] args = new String[3];
        args[0] = s1TxtFilePath;
        args[1] = s1OutputPath.concat("hash_").concat(s1TxtFilePath).concat(FileType.GPG.getExtension());
        args[2] = s1OutputPath;
        try {
            CustomHashValidation.hashValidation(args);
        } catch(Exception e) {
            throw new HashProgramValidationException(e.getMessage());
        }

        File directoryPath = new File(s1OutputPath);
        String errFileName = "Error_".concat(s1TxtFilePath);
        if (null != directoryPath.list() && Arrays.asList(directoryPath.list()).contains(errFileName)) {
            throw new InvalidDataException(Files.readAllLines(Paths.get(s1OutputPath.concat(errFileName))));
        }
        return Paths.get(args[1]).toFile();
    }

    @Override
    public File generateCtrlFile(String s1FileName) throws IOException {
        String ctrlFileName = CommonUtils.replaceLast(s1FileName, FileType.TXT_GPG.getExtension(), FileType.CTRL.getExtension());
        log.info("generating ctrl file {}", ctrlFileName);
        File ctrlFile = new File(ctrlFileName);
        try (
             OutputStream fos = new FileOutputStream(ctrlFile);
             Writer fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)
        ) {
            fw.append(s1FileName);
        }
        return ctrlFile;
    }
}
