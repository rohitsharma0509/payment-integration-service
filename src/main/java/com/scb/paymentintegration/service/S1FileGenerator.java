package com.scb.paymentintegration.service;

import com.scb.paymentintegration.dto.RiderSettlementRequest;

import java.io.File;
import java.io.IOException;

public interface S1FileGenerator {
    File generateS1File(RiderSettlementRequest riderSettlementRequest) throws IOException;
    File generateS1FileWithHash(String s1FileName) throws IOException;
    File generateCtrlFile(String s1FileName) throws IOException;
}
