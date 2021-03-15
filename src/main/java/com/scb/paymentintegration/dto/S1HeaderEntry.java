package com.scb.paymentintegration.dto;

import com.scb.paymentintegration.constants.Constants;
import com.scb.paymentintegration.util.CommonUtils;
import com.scb.paymentintegration.util.S1FileUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Builder
public class S1HeaderEntry implements S1FileEntry {
    private String recordType;
    private String companyId;
    private String batchReferenceNumber;
    private String channelId;

    public static S1HeaderEntry of(String companyId, String batchReferenceNumber) {
        return S1HeaderEntry.builder()
                .recordType("001")
                .companyId(companyId)
                .batchReferenceNumber(batchReferenceNumber)
                .channelId("BCM")
                .build();
    }

    @Override
    public String toS1FormattedString() {
        StringBuilder headerSb = new StringBuilder("");
        headerSb.append(recordType);//record type
        headerSb.append(S1FileUtils.toS1FormattedString(companyId, 12));//SCB Company Id
        headerSb.append(S1FileUtils.toS1FormattedString(batchReferenceNumber, 32));//Customer Reference or Description *(can use only first 12 digit)
        headerSb.append(CommonUtils.getFormattedCurrentDateTime(Constants.DATETIME_FORMAT_YYYYMMDDHHMMSS));//File date and time
        headerSb.append(channelId);//Channel Id
        headerSb.append(S1FileUtils.toS1FormattedString(batchReferenceNumber, 32));//Batch Reference
        log.info("header details string length: {} ", headerSb.length());
        return headerSb.toString();
    }
}
