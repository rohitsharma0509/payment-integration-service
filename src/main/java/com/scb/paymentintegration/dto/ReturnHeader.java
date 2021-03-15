package com.scb.paymentintegration.dto;

import com.scb.paymentintegration.exception.InvalidDataException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Getter
@Setter
@Builder
public class ReturnHeader {

    private static final String BATCH_PREFIX = "S1";

    private String recordType;
    private String creationDate;
    private String creationTime;
    private String fileReference;
    private String companyId;
    private String paymentType;
    private String channelId;
    private String batchReferenceNumber;
    private String valueDate;

    public static ReturnHeader of(String headerString) {
        if(StringUtils.isEmpty(headerString)) {
            throw new InvalidDataException("header is either empty or null.");
        }
        if(headerString.length() != 194) {
            throw new InvalidDataException("header length: expected: 194, actual: " + headerString.length());
        }
        String combinedReferences = headerString.substring(17, 49).trim();
        return ReturnHeader.builder()
                .recordType(headerString.substring(0, 3).trim())
                .creationDate(headerString.substring(3, 11).trim())
                .creationTime(headerString.substring(11, 17).trim())
                .fileReference(getScbReferenceNumber(combinedReferences))
                .companyId(headerString.substring(49, 61).trim())
                .paymentType(headerString.substring(61, 131).trim())
                .channelId(headerString.substring(131, 151).trim())
                .batchReferenceNumber(getBatchReferenceNumber(combinedReferences))
                .valueDate(headerString.substring(186, 194).trim())
                .build();
    }

    private static String getScbReferenceNumber(String fileReference) {
        return fileReference.substring(0, fileReference.indexOf(BATCH_PREFIX));
    }

    private static String getBatchReferenceNumber(String fileReference) {
        return fileReference.substring(fileReference.indexOf(BATCH_PREFIX));
    }

}
