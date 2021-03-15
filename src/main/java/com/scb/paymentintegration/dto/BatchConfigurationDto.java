package com.scb.paymentintegration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchConfigurationDto {
    private String rhDebitAccNum;
    private String companyId;
    private String productCode;
    private String feeDebitAccNum;
    private String rhCutOffTime;
    private String settlementFilePath;
    private String s1InputFolderFilePath;
    private String s1OutputFolderFilePath;
    private String numOfMonths;
    private String taxDeductionPercentage;
    private String s1fileNameConvention;
    private String scbfileNameConvention;
}
