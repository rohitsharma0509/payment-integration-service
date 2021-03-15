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
public class ReturnTrailer {
    private String recordType;
    private String totalNumberOfTransactions;
    private Double totalPaymentAmount;

    public static ReturnTrailer of(String trailerString) {
        if(StringUtils.isEmpty(trailerString)) {
            throw new InvalidDataException("trailer is either empty or null.");
        }
        if(trailerString.length() != 29) {
            throw new InvalidDataException("trailer length: expected: 29, actual: " + trailerString.length());
        }
        return ReturnTrailer.builder()
                .recordType(trailerString.substring(0, 3).trim())
                .totalNumberOfTransactions(trailerString.substring(3, 11).trim())
                .totalPaymentAmount(S1FileUtils.toFormattedDoubleV14V4(trailerString.substring(11, 29).trim()))
                .build();
    }
}
