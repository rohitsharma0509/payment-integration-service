package com.scb.paymentintegration.dto;

import com.scb.paymentintegration.constants.Constants;
import com.scb.paymentintegration.util.CommonUtils;
import com.scb.paymentintegration.util.S1FileUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
@Setter
@Builder
public class S1DebitEntry implements S1FileEntry {
    private String recordType;
    private String productCode;
    private String rhDebitAccountNumber;
    private String accountType;
    private String debitBranchCode;
    private String debitCurrency;
    private Double totalDebitAmount;
    private String internalReference;
    private Integer noOfCredits;
    private String filter;
    private Character mediaClearingCycle;


    public static S1DebitEntry of(String rhDebitAccountNumber, Double totalDebitAmount, Integer noOfCredits) {
        return S1DebitEntry.builder()
                .recordType("002")
                .productCode("DCP")
                .rhDebitAccountNumber(rhDebitAccountNumber)
                .accountType("0" + rhDebitAccountNumber.charAt(3))
                .debitBranchCode("0" + rhDebitAccountNumber.substring(0, 3))
                .debitCurrency("THB")
                .totalDebitAmount(totalDebitAmount)
                .internalReference("01")
                .noOfCredits(noOfCredits)
                .filter(S1FileUtils.getStringWithSpace(9))
                .mediaClearingCycle(Constants.BLANK_CHAR)
                .build();
    }

    @Override
    public String toS1FormattedString() {
        StringBuilder debitDetailsSb = new StringBuilder("");
        debitDetailsSb.append(recordType);//record type
        debitDetailsSb.append(productCode);//Product code
        debitDetailsSb.append(S1FileUtils.getFormattedProcessingDate(Constants.DATE_FORMAT_YYYYMMDD));//Date of processing
        debitDetailsSb.append(S1FileUtils.toS1FormattedString(rhDebitAccountNumber, 25));//Robinhood account number
        debitDetailsSb.append(accountType);//Account Type of Debit Account ("0"+4th char)
        debitDetailsSb.append(debitBranchCode);//Debit Branch Code ("0"+1-3 char)
        debitDetailsSb.append(debitCurrency);//Debit Currency
        debitDetailsSb.append(S1FileUtils.toS1FormattedAmount(totalDebitAmount));//Debit Amount
        debitDetailsSb.append(StringUtils.leftPad(internalReference, 8, '0'));//internal reference
        debitDetailsSb.append(StringUtils.leftPad(String.valueOf(noOfCredits), 6, '0'));//Total number of credits
        debitDetailsSb.append(S1FileUtils.toS1FormattedString(rhDebitAccountNumber, 15));//Fee account number
        debitDetailsSb.append(filter);
        debitDetailsSb.append(mediaClearingCycle);
        debitDetailsSb.append(accountType);//Account Type
        debitDetailsSb.append(debitBranchCode);//Debit Branch Code
        log.info("debit details string length: {}", debitDetailsSb.length());
        return debitDetailsSb.toString();
    }
}
