package com.scb.paymentintegration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderSettlementRequest {
    @NotNull(message = "{api.payment.integration.null.msg}")
    private Double totalConsolidatedAmount;
    @NotNull(message = "{api.payment.integration.null.msg}")
    private Integer noOfCredits;
    @NotNull(message = "{api.payment.integration.null.msg}")
    private Double totalDebitAmount;
    @NotBlank(message = "{api.payment.integration.blank.msg}")
    private String batchReferenceNumber;
    private List<RiderSettlementDetails> riderSettlementDetails;
}
