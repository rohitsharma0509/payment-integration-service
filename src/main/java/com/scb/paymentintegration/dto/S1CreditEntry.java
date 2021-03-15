package com.scb.paymentintegration.dto;

import com.scb.paymentintegration.constants.Constants;
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
public class S1CreditEntry implements S1FileEntry {

    private String recordType;
    private Integer sequenceNumber;
    private String riderAccountNumber;
    private Double totalCreditAmount;
    private String creditCurrency;
    private String internalReference;
    private Character whtPresent;
    private Character invoiceDetailsPresent;
    private Character creditAdviceRequired;
    private Character deliveryMode;
    private String pickupLocation;
    private String whtFormType;
    private String whtTaxRunningNo;
    private String whtAttachNo;
    private String noOfWhtDetails;
    private String totalWhtAmount;
    private String noOfInvoiceDetails;
    private String totalInvoiceAmount;
    private Character whtPayType;
    private String whtRemark;
    private String whtDeductDate;
    private String receivingBankCode;
    private String receivingBankName;
    private String receivingBranchCode;
    private String receivingBranchName;
    private Character whtSignatory;
    private Character beneficiaryNotification;
    private String customerReferenceNumber;
    private Character chequeReferenceDocumentType;
    private String paymentTypeCode;
    private String servicesType;
    private String remark;
    private String scbRemark;
    private String beneficiaryCharge;

    public static S1CreditEntry of(RiderSettlementDetails riderSettlementDetails, int seq) {
        return S1CreditEntry.builder()
                .recordType("003")
                .sequenceNumber(seq)
                .riderAccountNumber(riderSettlementDetails.getRiderAccountNumber())
                .totalCreditAmount(riderSettlementDetails.getTotalCreditAmount())
                .creditCurrency("THB")
                .internalReference("01")
                .whtPresent('N')
                .invoiceDetailsPresent('N')
                .creditAdviceRequired('N')
                .deliveryMode('S')
                .pickupLocation(S1FileUtils.getStringWithSpace(4))
                .whtFormType(S1FileUtils.getStringWithSpace(2))
                .whtTaxRunningNo(S1FileUtils.getStringWithSpace(14))
                .whtAttachNo(S1FileUtils.getStringWithSpace(6))
                .noOfWhtDetails(S1FileUtils.getStringWithSpace(2))
                .totalWhtAmount("0000000000000000")
                .noOfInvoiceDetails(S1FileUtils.getStringWithSpace(6))
                .totalInvoiceAmount("0000000000000000")
                .whtPayType(Constants.BLANK_CHAR)
                .whtRemark(S1FileUtils.getStringWithSpace(40))
                .whtDeductDate(S1FileUtils.getStringWithSpace(8))
                .receivingBankCode("014")
                .receivingBankName(S1FileUtils.getStringWithSpace(35))
                .receivingBranchCode("0"+ riderSettlementDetails.getRiderAccountNumber().substring(0, 3))
                .receivingBranchName(S1FileUtils.getStringWithSpace(35))
                .whtSignatory(Constants.BLANK_CHAR)
                .beneficiaryNotification('N')
                .customerReferenceNumber(StringUtils.isNotBlank(riderSettlementDetails.getRiderId()) ? riderSettlementDetails.getRiderId() : "")
                .chequeReferenceDocumentType(Constants.BLANK_CHAR)
                .paymentTypeCode(S1FileUtils.getStringWithSpace(3))
                .servicesType(S1FileUtils.getStringWithSpace(2))
                .remark(S1FileUtils.getStringWithSpace(50))
                .scbRemark(S1FileUtils.getStringWithSpace(18))
                .beneficiaryCharge(S1FileUtils.getStringWithSpace(2))
                .build();
    }

    @Override
    public String toS1FormattedString() {
        StringBuilder creditDetailsSb = new StringBuilder("");
        creditDetailsSb.append(recordType);//record type
        creditDetailsSb.append(StringUtils.leftPad(String.valueOf(sequenceNumber), 6, '0'));//Credit sequence number
        creditDetailsSb.append(S1FileUtils.toS1FormattedString(riderAccountNumber, 25));//Credit Account
        creditDetailsSb.append(S1FileUtils.toS1FormattedAmount(totalCreditAmount));//Credit Amount
        creditDetailsSb.append(creditCurrency);//Credit Currency
        creditDetailsSb.append(StringUtils.leftPad(internalReference, 8, '0'));//Internal Reference
        creditDetailsSb.append(whtPresent);//WHT Present
        creditDetailsSb.append(invoiceDetailsPresent);//Invoice Details Present
        creditDetailsSb.append(creditAdviceRequired);//Credit Advice Required
        creditDetailsSb.append(deliveryMode);//Delivery Mode
        creditDetailsSb.append(pickupLocation);//Pickup Location
        creditDetailsSb.append(whtFormType);//WHT Form Type
        creditDetailsSb.append(whtTaxRunningNo);//WHT Tax Running No.
        creditDetailsSb.append(whtAttachNo);//WHT Attach No.
        creditDetailsSb.append(noOfWhtDetails);//No. of WHT Details
        creditDetailsSb.append(totalWhtAmount);//Total WHT Amount
        creditDetailsSb.append(noOfInvoiceDetails);//No. of Invoice Details
        creditDetailsSb.append(totalInvoiceAmount);//Total Invoice Amount
        creditDetailsSb.append(whtPayType);//WHT Pay Type
        creditDetailsSb.append(whtRemark);//WHT Remark
        creditDetailsSb.append(whtDeductDate);//WHT Deduct Date
        creditDetailsSb.append(receivingBankCode);//Receiving Bank Code
        creditDetailsSb.append(receivingBankName);//Receiving Bank Name
        creditDetailsSb.append(receivingBranchCode);//Receiving Branch Code (0+first 3 digits of rider account number)
        creditDetailsSb.append(receivingBranchName);//Receiving Branch Name
        creditDetailsSb.append(whtSignatory);//WHT Signatory
        creditDetailsSb.append(beneficiaryNotification);//Beneficiary Notification
        creditDetailsSb.append(S1FileUtils.toS1FormattedString(customerReferenceNumber, 20));//Rider referenceNumber
        creditDetailsSb.append(chequeReferenceDocumentType);//Cheque Reference Document Type
        creditDetailsSb.append(paymentTypeCode);//Payment Type Code
        creditDetailsSb.append(servicesType);//ServicesType
        creditDetailsSb.append(remark);//Remark
        creditDetailsSb.append(scbRemark);//SCB Remark
        creditDetailsSb.append(beneficiaryCharge);//Beneficiary Charge
        log.info("credit details string length: {}", creditDetailsSb.length());
        return creditDetailsSb.toString();
    }
}
