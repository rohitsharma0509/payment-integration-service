package com.scb.paymentintegration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderSettlementDetails {
    private String riderId;
    private String riderName;
    @NotBlank(message = "{api.payment.integration.blank.msg}")
    private String riderAccountNumber;
    private Double totalCreditAmount;
}
