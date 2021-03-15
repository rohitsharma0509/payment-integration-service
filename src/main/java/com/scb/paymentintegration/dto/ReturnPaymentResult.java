package com.scb.paymentintegration.dto;

import com.scb.paymentintegration.exception.InvalidDataException;
import com.scb.paymentintegration.util.S1FileUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Getter
@Setter
@Builder
public class ReturnPaymentResult {
    private String recordType;
    private String paymentCurrency;
    private Double paymentAmount;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private String beneficiaryTaxId;
    private String beneficiaryBankName;
    private String beneficiaryBankCode;
    private String beneficiaryBranchName;
    private String beneficiaryBranchCode;
    private String bankReference;
    private String processingStatus;
    private String processingRemarks;
    private Double netPaymentAmount;
    private String chequeNumber;
    private String customerReferenceNumber;
    private String whtSerialNumber;
    private String personalId;
    private String chequeNumber1;

    public static ReturnPaymentResult of(String resultString) {
        if(StringUtils.isEmpty(resultString)) {
            throw new InvalidDataException("rider payment result is either empty or null.");
        }
        if(resultString.length() != 425) {
            throw new InvalidDataException("rider payment result length: expected: 425, actual: " + resultString.length());
        }
        return ReturnPaymentResult.builder()
                .recordType(resultString.substring(0, 3).trim())
                .paymentCurrency(resultString.substring(3, 6).trim())
                .paymentAmount(S1FileUtils.toFormattedDouble(resultString.substring(6, 22).trim()))
                .beneficiaryAccount(resultString.substring(22, 47).trim())
                .beneficiaryName(resultString.substring(47, 117).trim())
                .beneficiaryTaxId(resultString.substring(117, 127).trim())
                .beneficiaryBankName(resultString.substring(127, 162).trim())
                .beneficiaryBankCode(resultString.substring(162, 165).trim())
                .beneficiaryBranchName(resultString.substring(165, 200).trim())
                .beneficiaryBranchCode(resultString.substring(200, 204).trim())
                .bankReference(resultString.substring(204, 220).trim())
                .processingStatus(resultString.substring(220, 221).trim())
                .processingRemarks(resultString.substring(221, 345).trim())
                .netPaymentAmount(S1FileUtils.toFormattedDouble(resultString.substring(345, 361).trim()))
                .chequeNumber(resultString.substring(361, 368).trim())
                .customerReferenceNumber(resultString.substring(368, 388).trim())
                .whtSerialNumber(resultString.substring(388, 402).trim())
                .personalId(resultString.substring(402, 417).trim())
                .chequeNumber(resultString.substring(417, 425).trim())
                .build();
    }

}
