package com.scb.paymentintegration.service.impl;

import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.enums.FileType;
import com.scb.paymentintegration.service.CryptoService;
import com.scb.paymentintegration.service.FileHandlerService;
import com.scb.paymentintegration.service.S1FileGenerator;
import com.scb.paymentintegration.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
@Service
public class FileHandlerServiceImpl implements FileHandlerService {

    @Autowired
    private S1FileGenerator s1FileGenerator;

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Autowired
    private CryptoService cryptoService;

    @Value("${amazon.s3.bucket-name.s1-file}")
    private String s1FileBucketName;

    @Value("${crypto.public-key}")
    private String publicKeyPath;

    @Value("${s1.hashing.output-path}")
    private String s1OutputPath;

    @Override
    public String generateAndUploadS1File(RiderSettlementRequest riderSettlementRequest) throws IOException {
        File s1File = null;
        File ctrlFile = null;
        File s1FileWithHash = null;
        File encryptedFile = null;
        String s1FileUrl = null;
        try {
            s1File = s1FileGenerator.generateS1File(riderSettlementRequest);
            s1FileWithHash = s1FileGenerator.generateS1FileWithHash(s1File.getName());
            String encryptedFileName = s1File.getName().concat(FileType.GPG.getExtension());
            ctrlFile = s1FileGenerator.generateCtrlFile(encryptedFileName);
            log.info("encrypting s1 file using public key {}", publicKeyPath);
            encryptedFile = cryptoService.encrypt(s1FileWithHash, publicKeyPath, encryptedFileName, Boolean.FALSE);
            String ctrlFileUrl = amazonS3Service.uploadFile(ctrlFile.getName(), ctrlFile, s1FileBucketName, riderSettlementRequest.getBatchReferenceNumber());
            log.info("File uploaded successfully to s3. ctrl file name {}", ctrlFileUrl);
            s1FileUrl = amazonS3Service.uploadFile(encryptedFile.getName(), encryptedFile, s1FileBucketName, riderSettlementRequest.getBatchReferenceNumber());
            log.info("File uploaded successfully to s3. s1 file name {}", s1FileUrl);
        } finally {
            if(Objects.nonNull(s1File)) Files.deleteIfExists(s1File.toPath());
            if(Objects.nonNull(ctrlFile)) Files.deleteIfExists(ctrlFile.toPath());
            if(Objects.nonNull(s1FileWithHash)) Files.deleteIfExists(s1FileWithHash.toPath());
            if(Objects.nonNull(encryptedFile)) Files.deleteIfExists(encryptedFile.toPath());
            FileUtils.cleanDirectory(s1OutputPath);
        }
        return s1FileUrl;
    }
}
