package com.scb.paymentintegration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SftpRequest {
    private String batchReferenceNumber;
    private String fileName;
    private String sftpDestinationDir;
}
