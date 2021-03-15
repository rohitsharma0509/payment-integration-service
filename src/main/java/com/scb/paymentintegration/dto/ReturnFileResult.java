package com.scb.paymentintegration.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReturnFileResult {
    private ReturnHeader header;
    private List<ReturnPaymentResult> paymentResults;
    private ReturnTrailer trailer;

    public static ReturnFileResult of(ReturnHeader header, List<ReturnPaymentResult> paymentResults, ReturnTrailer trailer) {
        return ReturnFileResult.builder()
                .header(header)
                .paymentResults(paymentResults)
                .trailer(trailer)
                .build();
    }
}
