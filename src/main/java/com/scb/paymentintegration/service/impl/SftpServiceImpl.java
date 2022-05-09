package com.scb.paymentintegration.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.scb.paymentintegration.client.handler.OperationServiceClient;
import com.scb.paymentintegration.client.SettlementFeignClient;
import com.scb.paymentintegration.client.SftpClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.dto.ReturnHeader;
import com.scb.paymentintegration.dto.ReturnFileResult;
import com.scb.paymentintegration.dto.ReturnPaymentResult;
import com.scb.paymentintegration.dto.ReturnTrailer;
import com.scb.paymentintegration.dto.SftpRequest;
import com.scb.paymentintegration.enums.FileType;
import com.scb.paymentintegration.exception.EmptyReturnFileException;
import com.scb.paymentintegration.exception.InvalidDataException;
import com.scb.paymentintegration.service.CryptoService;
import com.scb.paymentintegration.service.SftpService;
import com.scb.paymentintegration.util.CommonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SftpServiceImpl implements SftpService {

    private static final String PROCESSED = "/processed/";
    private static final String ERROR = "/error/";

    @Value("${amazon.s3.bucket-name.s1-file}")
    private String s1FileBucketName;

    @Value("${crypto.private-key}")
    private String privateKeyPath;

    private String passphrase;

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Autowired
    private OperationServiceClient operationServiceClient;

    @Autowired
    private SettlementFeignClient settlementFeignClient;

    @Autowired
    private SftpClient sftpClient;

    @Autowired
    private CryptoService cryptoService;

    @Value("${secretsPath}")
    private String secretsPath;

    @SneakyThrows
    @PostConstruct
    public void setPasswordPhrase() {
        URI mongoUriPath = ResourceUtils.getURL(secretsPath + "/CRYPTO_PASSPHRASE").toURI();
        String passphrase = CommonUtils.sanitize(Files.readAllBytes(Paths.get(mongoUriPath)));
        this.passphrase = passphrase;
    }

    @Override
    public boolean uploadFile(SftpRequest sftpRequest) throws IOException {
        BatchConfigurationDto batchConfig = operationServiceClient.getBatchConfiguration();
        String s1FileName = sftpRequest.getFileName();
        String ctrlFileName = CommonUtils.replaceLast(s1FileName, FileType.TXT_GPG.getExtension(), FileType.CTRL.getExtension());

        log.info("Trying to download from s3 s1FileName {} & ctrlFileName {}", s1FileName, ctrlFileName);
        log.info("s3 details - bucketName: {}, key: {}", s1FileBucketName, sftpRequest.getBatchReferenceNumber());
        byte[] s1FileBytes = amazonS3Service.downloadFile(sftpRequest.getFileName(), s1FileBucketName, sftpRequest.getBatchReferenceNumber());
        byte[] ctrlFileBytes = amazonS3Service.downloadFile(ctrlFileName, s1FileBucketName, sftpRequest.getBatchReferenceNumber());

        ChannelSftp channelSftp = sftpClient.connect();
        try (
                InputStream s1InputStream = new ByteArrayInputStream(s1FileBytes);
                InputStream ctrlInputStream = new ByteArrayInputStream(ctrlFileBytes);
        ) {
            String destinationDir = batchConfig.getS1InputFolderFilePath();
            if (!StringUtils.isEmpty(sftpRequest.getSftpDestinationDir())) {
                destinationDir = sftpRequest.getSftpDestinationDir();
            }
            log.info("Uploading s1FileName {} & ctrlFileName {} on sftp. destinationDir: {}", s1FileName, ctrlFileName, destinationDir);
            channelSftp.put(ctrlInputStream, destinationDir.concat(ctrlFileName));
            channelSftp.put(s1InputStream, destinationDir.concat(s1FileName));
            return true;
        } catch (SftpException e) {
            log.error("Exception while uploading file on sftp", e);
        } finally {
            sftpClient.disconnect(channelSftp);
        }

        return false;
    }

    @Override
    public boolean pollOutputPath() {
        BatchConfigurationDto batchConfig = operationServiceClient.getBatchConfiguration();
        try {
            List<ChannelSftp.LsEntry> list = getListOfFilesToProcess(batchConfig);
            if (CollectionUtils.isEmpty(list)) {
                log.info("There are no ctrl files available on output path {}", batchConfig.getS1OutputFolderFilePath());
                return false;
            }

            for (ChannelSftp.LsEntry entry : list) {
                String ctrlFileName = entry.getFilename();
                log.info("processing ctrlFileName: {}", ctrlFileName);
                String returnFileName = CommonUtils.replaceLast(ctrlFileName, FileType.CTRL.getExtension(), FileType.TXT_GPG.getExtension());
                log.info("return file name: {}", returnFileName);
                parse(ctrlFileName, returnFileName, batchConfig.getS1OutputFolderFilePath());
            }
            return Boolean.TRUE;
        } catch(Exception e) {
            log.error("Exception while polling return file path:  {}", e);
        }
        return Boolean.FALSE;
    }

    private List<ChannelSftp.LsEntry> getListOfFilesToProcess(BatchConfigurationDto batchConfig) throws SftpException {
        ChannelSftp channelSftp = sftpClient.connect();
        try {
            log.info("current directory : {}", channelSftp.pwd());
            channelSftp.cd(batchConfig.getS1OutputFolderFilePath());
            log.info("Current directory {}, Moved to outputPath {}", channelSftp.pwd(), batchConfig.getS1OutputFolderFilePath());
            return channelSftp.ls(batchConfig.getCompanyId().concat("*.ctrl"));
        } finally {
            sftpClient.disconnect(channelSftp);
        }
    }

    private void move(String toPath, List<String> fileNames, String configuredReturnFilePath) throws SftpException {
        ChannelSftp channelSftp = sftpClient.connect();
        channelSftp.cd(configuredReturnFilePath);
        for(String fileName : fileNames) {
            move(channelSftp, toPath, fileName);
        }
        sftpClient.disconnect(channelSftp);
    }

    private void move(ChannelSftp channelSftp, String toPath, String fileName) throws SftpException {
        if(!StringUtils.isEmpty(fileName)) {
            String currentDir = channelSftp.pwd();
            log.info("Current directory {}, Moving {} to {} folder", currentDir, fileName, toPath);
            channelSftp.rename(fileName, getDestinationPath(currentDir, toPath, fileName));
        }
    }

    private String getDestinationPath(String currentDir, String toPath, String fileName) {
        StringBuilder path = new StringBuilder(currentDir);
        path.append(toPath).append(fileName);
        return path.toString();
    }

    private void parse(String ctrlFileName, String returnFileName, String configuredReturnFilePath) throws SftpException {
        List<String> files = Arrays.asList(ctrlFileName, returnFileName);
        try {
            ReturnFileResult result = parseEncryptedReturnFile(returnFileName, configuredReturnFilePath);
            if (Objects.nonNull(result)) {
                boolean isSaved = settlementFeignClient.saveReturnFileResponse(result);
                log.info("isSaved to settlement service: {}", isSaved);
                if (isSaved) {
                    move(PROCESSED, files, configuredReturnFilePath);
                }
            }
        } catch(EmptyReturnFileException e) {
            log.info("{}, file name {}", e.getMessage(), returnFileName);
            move(PROCESSED, files, configuredReturnFilePath);
        } catch(InvalidDataException e) {
            e.getMessages().forEach(log::error);
            move(ERROR, files, configuredReturnFilePath);
        } catch(Exception e) {
            log.error("Exception while parsing return file: {}, {}", returnFileName, e);
        }
    }

    private ReturnFileResult parseEncryptedReturnFile(String returnFileName, String configuredReturnFilePath) throws IOException, SftpException {
        ChannelSftp channelSftp = sftpClient.connect();
        channelSftp.cd(configuredReturnFilePath);
        try (
            InputStream encryptedFileInputStream = channelSftp.get(returnFileName);
            InputStream decryptedFileInputStream = cryptoService.decrypt(encryptedFileInputStream, privateKeyPath, passphrase.toCharArray());
            InputStreamReader retInputStreamReader = new InputStreamReader(decryptedFileInputStream, StandardCharsets.UTF_8);
            BufferedReader returnFileReader = new BufferedReader(retInputStreamReader);
        ) {
            if(!returnFileReader.ready()) {
                throw new EmptyReturnFileException("Return file is empty");
            }
            String headerLine = returnFileReader.readLine();
            ReturnHeader header = ReturnHeader.of(headerLine);
            ReturnTrailer trailer = null;
            List<ReturnPaymentResult> paymentResults = new ArrayList<>();
            String line;
            while ((line = returnFileReader.readLine()) != null) {
                if (line.startsWith("599")) {
                    trailer = ReturnTrailer.of(line);
                    break;
                } else {
                    paymentResults.add(ReturnPaymentResult.of(line));
                }
            }
            log.info("received return file for batchId {}, noOfRiders {}", header.getBatchReferenceNumber(), paymentResults.size());
            return ReturnFileResult.of(header, paymentResults, trailer);
        } finally {
            sftpClient.disconnect(channelSftp);
        }
    }
}
