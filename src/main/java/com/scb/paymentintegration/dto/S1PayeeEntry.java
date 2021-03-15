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
public class S1PayeeEntry implements S1FileEntry {
    private String recordType;
    private String internalReference;
    private Integer sequenceNumber;
    private String payee1IdCard;
    private String payee1NameThai;
    private String payee1Address1;
    private String payee1Address2;
    private String payee1Address3;
    private String payee1TaxId;
    private String payee1NameEnglish;
    private String payee1FaxNumber;
    private String payee1MobilePhoneNumber;
    private String payee1EmailAddress;
    private String payee2NameThai;
    private String payee2Address1;
    private String payee2Address2;
    private String payee2Address3;

    public static S1PayeeEntry of(int seq) {
        return S1PayeeEntry.builder()
                .recordType("004")
                .internalReference("01")
                .sequenceNumber(seq)
                .payee1IdCard(S1FileUtils.getStringWithSpace(15))
                .payee1NameThai(S1FileUtils.getStringWithSpace(100))
                .payee1Address1(S1FileUtils.getStringWithSpace(70))
                .payee1Address2(S1FileUtils.getStringWithSpace(70))
                .payee1Address3(S1FileUtils.getStringWithSpace(70))
                .payee1TaxId(S1FileUtils.getStringWithSpace(10))
                .payee1NameEnglish(S1FileUtils.getStringWithSpace(70))
                .payee1FaxNumber(S1FileUtils.getStringWithSpace(10))
                .payee1MobilePhoneNumber(S1FileUtils.getStringWithSpace(10))
                .payee1EmailAddress(S1FileUtils.getStringWithSpace(64))
                .payee2NameThai(S1FileUtils.getStringWithSpace(100))
                .payee2Address1(S1FileUtils.getStringWithSpace(70))
                .payee2Address2(S1FileUtils.getStringWithSpace(70))
                .payee2Address3(S1FileUtils.getStringWithSpace(70))
                .build();

    }

    @Override
    public String toS1FormattedString() {
        StringBuilder payeeDetailsSb = new StringBuilder("");
        payeeDetailsSb.append(recordType);//record type
        payeeDetailsSb.append(StringUtils.leftPad(internalReference, 8, '0'));//Internal Reference
        payeeDetailsSb.append(StringUtils.leftPad(String.valueOf(sequenceNumber), 6, '0'));//Credit sequence number
        payeeDetailsSb.append(payee1IdCard);//Payee1 IDCard
        payeeDetailsSb.append(payee1NameThai);//Payee1 Name (Thai)
        payeeDetailsSb.append(payee1Address1);//Payee1 Address 1
        payeeDetailsSb.append(payee1Address2);//Payee1 Address 2
        payeeDetailsSb.append(payee1Address3);//Payee1 Address 3
        payeeDetailsSb.append(payee1TaxId);//Payee1 Tax ID
        payeeDetailsSb.append(payee1NameEnglish);//Payee1 Name (English)
        payeeDetailsSb.append(payee1FaxNumber);//Payee1 Fax Number
        payeeDetailsSb.append(payee1MobilePhoneNumber);//Payee1 Mobile Phone Number
        payeeDetailsSb.append(payee1EmailAddress);//Payee1 E-mail Address
        payeeDetailsSb.append(payee2NameThai);//Payee2 Name (Thai)
        payeeDetailsSb.append(payee2Address1);//Payee2 Address 1
        payeeDetailsSb.append(payee2Address2);//Payee2 Address 2
        payeeDetailsSb.append(payee2Address3);//Payee2 Address 3
        log.info("payee details string length: {}", payeeDetailsSb.length());
        return payeeDetailsSb.toString();
    }
}
