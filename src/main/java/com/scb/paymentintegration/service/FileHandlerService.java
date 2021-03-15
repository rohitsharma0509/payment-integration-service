package com.scb.paymentintegration.service;

import com.scb.paymentintegration.dto.RiderSettlementRequest;

import java.io.IOException;

public interface FileHandlerService {
    String generateAndUploadS1File(RiderSettlementRequest riderSettlementRequest) throws IOException;
}
