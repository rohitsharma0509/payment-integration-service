package com.scb.paymentintegration.dto;

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
public class S1TrailerEntry implements S1FileEntry {
    private String recordType;
    private Integer totalNoOfDebits;
    private Integer totalNoOfCredits;
    private Double totalAmount;

    public static S1TrailerEntry of(Double totalConsolidatedAmount, Integer noOfRiders) {
        return S1TrailerEntry.builder()
                .recordType("999")
                .totalNoOfDebits(1)
                .totalNoOfCredits(noOfRiders)
                .totalAmount(totalConsolidatedAmount)
                .build();
    }

    @Override
    public String toS1FormattedString() {
        StringBuilder trailerDetailsSb = new StringBuilder("");
        trailerDetailsSb.append(recordType);//Record Type
        trailerDetailsSb.append(StringUtils.leftPad(String.valueOf(totalNoOfDebits), 6, '0'));//Total No. of Debits
        trailerDetailsSb.append(StringUtils.leftPad(String.valueOf(totalNoOfCredits), 6, '0'));//Total No. of Credits
        trailerDetailsSb.append(S1FileUtils.toS1FormattedAmount(totalAmount));//Total Amount
        log.info("trailer details string length: {}", trailerDetailsSb.length());
        return trailerDetailsSb.toString();
    }
}
