package com.scb.paymentintegration.service;

import com.scb.paymentintegration.dto.SftpRequest;

import java.io.IOException;

public interface SftpService {
    boolean uploadFile(SftpRequest sftpRequest) throws IOException;

    boolean pollOutputPath();
}
