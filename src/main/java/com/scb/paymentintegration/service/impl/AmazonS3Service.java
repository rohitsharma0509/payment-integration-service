package com.scb.paymentintegration.service.impl;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

@Log4j2
@Service
public class AmazonS3Service extends AmazonClientService {

    private static final String PAYMENT = "payment/";

    public String uploadFile(String fileName, File file, String bucketName, String key) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.length());
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        try (InputStream inputStream = new FileInputStream(file)) {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, getKey(key, fileName), inputStream, objectMetadata);
            getClient().putObject(putRequest);
        } catch(IOException e) {
            log.error("Exception while uploading in s3. File not found : {}", e);
        }
        return fileName;
    }

    public byte[] downloadFile(String fileName, String bucketName, String key) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, getKey(key, fileName));
        S3Object object = getClient().getObject(getObjectRequest);
        return IOUtils.toByteArray(object.getObjectContent());
    }
    
    private String getKey(String key, String fileName) {
        StringBuilder sb = new StringBuilder(PAYMENT);
        sb.append(key).append("/").append(fileName);
        return sb.toString();
    }

}
