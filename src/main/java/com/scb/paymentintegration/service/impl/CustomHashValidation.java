package com.scb.paymentintegration.service.impl;

import com.scb.hashAllSys.formatter.DateFormatter;
import com.scb.hashAllSys.report.format.HashPdfReportFormat;
import com.scb.hashAllSys.report.value.CreditInstructionValue;
import com.scb.hashAllSys.report.value.HashPdfValue;
import com.scb.hashAllSys.report.value.PaymentTransactionValue;
import com.scb.hashAllSys.util.DateUtil;
import com.scb.hashAllSys.util.DomesticPaymentUtil;
import com.scb.hashAllSys.util.NumericUtil;
import com.scb.hashAllSys.util.SHAConv;
import com.scb.hashAllSys.util.TextUtil;
import com.scb.hashAllSys.validations.CommonValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class CustomHashValidation extends CommonValidation {
    private String inputFilePath;
    private String outputFilePath;
    private String reportPath;
    private String reportFilePath;
    private final String ERRORFILE_SUFFIX = ".txt";
    private BigDecimal sumCreditAmount;
    private BigDecimal sumWHTAmount;
    private BigDecimal sumWHTAmountOverInv;
    private BigDecimal sumAmount;
    private String debitInternalReference;
    private String whtPresent;
    private String chqNotification;
    private String whtIncomeType;
    private int numDebit;
    private int numCredit;
    private Map<String, BigDecimal> mapEWhtAmount005;
    private Map<String, Integer> mapEWhtAmount005Line;
    private Map<String, BigDecimal> mapEWhtAmount006;
    private Map<String, Integer> mapEWhtAmount006Line;
    private HashPdfValue pdfvalue;
    private static List<String> pickuplocationlist;

    public CustomHashValidation() {
    }

    public static void hashValidation(String[] args) throws Exception {
        boolean result = true;

        log.debug("Hash Module Java Version 1.0");
        log.debug("Build 07.09.2020");
        CustomHashValidation instance = new CustomHashValidation();
        instance.validate(args);
        if (!instance.haveError) {
            instance.hash();
            instance.generatePDFReport();
        } else {
            result = false;
        }

    }

    private void validate(String[] params) throws Exception {
        initialAndValidateParam(params);
        loadProperties();
        readDataFile();
    }

    private void hash() throws Exception {
        if(StringUtils.isEmpty(builder)) {
            throw new Exception("ERROR: Input s1 file text is empty");
        }
        SHAConv hash = new SHAConv();
        String hashCode = "";
        String hashValue = hash.convert(builder.toString().getBytes(StandardCharsets.UTF_8));
        if(!StringUtils.isEmpty(hashValue)) {
            hashCode = hashValue.toUpperCase();
        }
        builder.insert(0, hashCode + this.NEWLINE);
        try(
            FileOutputStream  fos = new FileOutputStream(this.outputFilePath);
            OutputStreamWriter out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        ) {
            out.write(this.builder.toString());
        }
        this.pdfvalue.setHashCode(hashCode);
    }

    private void generatePDFReport() throws Exception {
        HashPdfReportFormat report = new HashPdfReportFormat();
        report.doGenerate(this.pdfvalue, this.reportFilePath);
    }

    private void initialAndValidateParam(String[] params) throws Exception {
        boolean isInsuffParams = false;
        if (params.length > 0) {
            if (params.length < 2) {
                isInsuffParams = true;
            } else if (params.length == 2) {
                inputFilePath = params[0];
                outputFilePath = params[1];
            } else if (params.length > 2) {
                inputFilePath = params[0];
                outputFilePath = params[1];
                reportPath = params[2];
            }
        } else {
            isInsuffParams = true;
        }

        if (isInsuffParams) {
            String errorMsg = "ERROR: Invalid parameters of Program \nParameters should be at least 2 parameters for full input and output file path. [Report/Error file path is optional]";
            throw new Exception(errorMsg);
        } else {
            File fileInput = new File(this.inputFilePath);
            String separator = retrieveLineSeparator(fileInput);
            if (separator != null) {
                NEWLINE = separator;
            }

            String errorMsg;
            if (!fileInput.exists()) {
                errorMsg = "ERROR: Input file not found. <" + this.inputFilePath + ">";
                throw new Exception(errorMsg);
            } else if (fileInput.length() == 0L) {
                errorMsg = "ERROR: Input file is empty.";
                throw new Exception(errorMsg);
            } else if(StringUtils.isEmpty(outputFilePath)) {
                throw new Exception("ERROR: outFilePath is null");
            } else {
                File fileOutput = new File(this.outputFilePath);
                if(StringUtils.isEmpty(fileOutput.getParent())) {
                    throw new Exception("ERROR: fileOutput parent is null");
                }
                File reportfile = new File(fileOutput.getParent());
                if (!reportfile.exists()) {
                    errorMsg = "ERROR: Path of output file not found. <" + fileOutput.getParent() + ">";
                    throw new Exception(errorMsg);
                } else {
                    if (reportPath == null || reportPath.equals("")) {
                        reportPath = fileOutput.getParent();
                    }

                    setReportFileNameAndPath(fileInput, fileOutput);
                    reportfile = new File(this.reportFilePath);
                    if (reportfile.exists()) {
                        if(!reportfile.delete()) {
                            log.error("Error while deleting reportFile");
                        }
                    }

                    File errorfile = new File(this.errorFilePath);
                    if (errorfile.exists()) {
                        if(!errorfile.delete()) {
                            log.error("Error while deleting errorfile");
                        }
                    }

                    if (!fileInput.getPath().equalsIgnoreCase(fileOutput.getPath()) && fileOutput.exists()) {
                        if(!fileOutput.delete()) {
                            log.error("Error while deleting fileOutput");
                        }
                    }

                    File fileReport = new File(this.reportPath);
                    if (!fileReport.exists()) {
                        errorMsg = "ERROR: Path of report file not found. <" + fileReport + ">";
                        throw new Exception(errorMsg);
                    } else {
                        this.pdfvalue = new HashPdfValue();
                        this.pdfvalue.setFileName(this.outputFilePath);
                    }
                }
            }
        }

    }

    public static String retrieveLineSeparator(File file) throws IOException {
        StringBuilder lineSeparator = new StringBuilder("");
        try(FileInputStream fis = new FileInputStream(file)) {
            while(fis.available() > 0) {
                char current = (char)fis.read();
                if (current == '\n' || current == '\r') {
                    lineSeparator.append(current);
                    if (fis.available() > 0) {
                        char next = (char)fis.read();
                        if (next != current && (next == '\r' || next == '\n')) {
                            lineSeparator.append(next);
                        }
                    }
                    return lineSeparator.toString();
                }
            }
        }
            return null;
    }

    private void setReportFileNameAndPath(File _fileInput, File _fileOutput) throws Exception {
        String inputTextFileName = _fileInput.getName().replaceAll(this.ERRORFILE_SUFFIX, "").replaceAll(this.ERRORFILE_SUFFIX.toUpperCase(), "");
        String outputTextFileName = _fileOutput.getName().replaceAll(this.ERRORFILE_SUFFIX, "").replaceAll(this.ERRORFILE_SUFFIX.toUpperCase(), "");
        this.reportFilePath = this.reportPath + "/Report_" + outputTextFileName + ".pdf";
        this.errorFilePath = this.reportPath + "/Error_" + inputTextFileName + this.ERRORFILE_SUFFIX;
    }

    private void loadProperties() throws Exception {
        try {
            if (pickuplocationlist == null) {
                pickuplocationlist = new ArrayList();
            }

            Properties prop = new Properties();
            Resource resource = new ClassPathResource("pickup.properties");
            prop.load(resource.getInputStream());
            Set pickupSet = prop.keySet();
            Iterator pickupIter = pickupSet.iterator();

            while(pickupIter.hasNext()) {
                String pickupLocation = (String)pickupIter.next();
                pickuplocationlist.add(pickupLocation);
            }

        } catch (FileNotFoundException var5) {
            throw var5;
        } catch (Exception var6) {
            String errorMsg = "ERROR: Unable to load pick up location properties file.";
            throw new Exception(errorMsg);
        }
    }

    private void readDataFile() throws Exception {
        try(
                InputStream fis = new FileInputStream(this.inputFilePath);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8.name());
                BufferedReader br = new BufferedReader(isr);
        ) {
            this.validateHeader(br);
            String recordType = null;
            this.detailline = this.readLine(br);
            if (this.detailline != null && !this.detailline.trim().equals("")) {
                recordType = this.getAndValidateRecordType(this.detailline);
                if (!recordType.equals("002")) {
                    String msgErr = "There is no debit detail record.";
                    this.writeError("Record Type", recordType, 1, 3, msgErr, recordType);
                    throw new Exception(this.getLineMsg(this.line) + msgErr);
                }

                while(recordType.equals("002")) {
                    this.validateDebitDetail(br);
                    this.detailline = this.readLine(br);
                    recordType = this.getAndValidateRecordType(this.detailline);
                }

                this.pdfvalue.setSumDebitAmount(this.sumAmount);
            }

            this.validateTrailer(br, recordType);
        }
    }

    private String getAndValidateRecordType(String line) throws Exception {
        int lineLength = line.length();
        String msgErr = null;
        String recordType = this.getString(line, 3);
        if (!recordType.equals("001") && !recordType.equals("002") && !recordType.equals("003") && !recordType.equals("004") && !recordType.equals("005") && !recordType.equals("006") && !recordType.equals("999")) {
            msgErr = "Can't not found Correct Record Type at beginning of line";
        } else if (recordType.equals("001") && lineLength != 96) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "001", 96, lineLength);
        } else if (recordType.equals("002") && lineLength != 109) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "002", 109, lineLength);
        } else if (recordType.equals("003") && lineLength != 355) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "003", 355, lineLength);
        } else if (recordType.equals("004") && lineLength != 816) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "004", 816, lineLength);
        } else if (recordType.equals("005") && lineLength != 138) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "005", 138, lineLength);
        } else if (recordType.equals("006") && lineLength != 196) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "006", 196, lineLength);
        } else if (recordType.equals("999") && lineLength != 31) {
            msgErr = String.format("Line%s should have range %d characters <%d>", "999", 31, lineLength);
        }

        if (msgErr != null) {
            this.writeError("Record Type", recordType, 1, 3, msgErr, recordType);
            throw new Exception(this.getLineMsg(this.line) + msgErr);
        } else {
            return recordType;
        }
    }

    private void validateHeader(BufferedReader _br) throws Exception {
        this.detailline = this.readLine(_br);
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            String recordType = this.getAndValidateRecordType(this.detailline);
            String corporateId = this.getString(this.detailline, 12);
            String customerReference = this.getString(this.detailline, 32);
            String msgDate = this.getString(this.detailline, 8);
            String msgTime = this.getString(this.detailline, 6);
            String channelCode = this.getString(this.detailline, 3);
            if (!recordType.equals("001")) {
                String msgErr = "There is no header record.";
                this.writeError("Record Type", recordType, 1, 3, msgErr, recordType);
                throw new Exception(this.getLineMsg(this.line) + msgErr);
            }

            this.initialize();
            this.validateNullorBlank("Company Id", recordType, 4, 15, corporateId);
            this.validateNullorBlank("Customer Reference", recordType, 16, 47, customerReference);
            this.validateDate("Message/File Date", recordType, 48, 55, msgDate, "yyyyMMdd", false);
            this.validateDate("Message/File Time", recordType, 56, 61, msgTime, "HHmmss", false);
            this.validateChannel(recordType, 62, 64, channelCode);
            this.pdfvalue.setCorporateId(corporateId);
            this.pdfvalue.setCustomerReference(customerReference);
        }

    }

    private void validateDebitDetail(BufferedReader _br) throws Exception {
        ++this.numDebit;
        this.sumCreditAmount = new BigDecimal(0);
        this.productCode = this.getString(this.detailline, 3);
        String valueDate = this.getString(this.detailline, 8);
        String debitAccountNo = this.getString(this.detailline, 25);
        this.getNext(this.detailline, 2);
        this.getNext(this.detailline, 4);
        String debitCurrency = this.getString(this.detailline, 3);
        String debitAmountStr = this.getAmount(this.detailline, 13, 3);
        this.debitInternalReference = this.getString(this.detailline, 8);
        String numCreditStr = this.getString(this.detailline, 6);
        String feeAccountNo = this.getString(this.detailline, 15);
        this.getNext(this.detailline, 9);
        String mclRound = this.getString(this.detailline, 1);
        this.validateProductCode("002", 4, 6, this.productCode);
        this.validateValueDate("Value Date", "002", 7, 14, valueDate, "yyyyMMdd", true, mclRound);
        this.validateAccount("Debit Account No.", "002", 15, 39, debitAccountNo);
        this.validateCurrency("Debit Currency", "002", 46, 48, debitCurrency);
        this.validateAmount("Debit Amount", "002", 49, 64, debitAmountStr, false, false);
        this.validateNullorBlank("Internal Reference", "002", 65, 72, this.debitInternalReference);
        this.validateNumeric("No. of Credit", "002", 73, 78, numCreditStr, 6);
        this.validateAccount("Fee Debit Account", "002", 79, 93, feeAccountNo);
        this.validateMCLCycle("002", 103, 103, mclRound);
        BigDecimal debitAmount = NumericUtil.parseBigDecimal(debitAmountStr);
        this.sumAmount = this.sumAmount.add(debitAmount);
        PaymentTransactionValue pmtValue = new PaymentTransactionValue();
        pmtValue.setProductCode(this.productCode);
        pmtValue.setMclCycle(mclRound);
        pmtValue.setDebitAccountNo(debitAccountNo);
        pmtValue.setValueDate(DateFormatter.parseDate(valueDate, "yyyyMMdd"));
        pmtValue.setAmount(debitAmount);
        int numCredits = NumericUtil.parseInt(numCreditStr);

        for(int i = 0; i < numCredits; ++i) {
            this.validateCreditDetail(_br, pmtValue);
        }

        if (debitAmount.compareTo(this.sumCreditAmount) != 0) {
            String msgErr = "Debit Amount is not equal summary of Credit Amount.";
            this.writeError("Debit Amount", "002", 49, 64, msgErr, debitAmount.toString());
        }

        this.pdfvalue.addPaymentTransactionValue(pmtValue);
    }

    private void validateCreditDetail(BufferedReader _br, PaymentTransactionValue _pmtValue) throws Exception {
        this.detailline = this.readLine(_br);
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            int lineforwht = this.line;
            String recordType = this.getAndValidateRecordType(this.detailline);
            String creditSeqNo = this.getString(this.detailline, 6);
            String creditAccountNo = this.getString(this.detailline, 25);
            String creditAmountStr = this.getAmount(this.detailline, 13, 3);
            String creditCurrency = this.getString(this.detailline, 3);
            String creditInternalReference = this.getString(this.detailline, 8);
            this.whtPresent = this.getString(this.detailline, 1);
            String invPresent = this.getString(this.detailline, 1);
            String isRequireCrAdvice = this.getString(this.detailline, 1);
            String deliveryMode = this.getString(this.detailline, 1);
            String pickupLocation = this.getString(this.detailline, 4);
            String whtFormType = this.getString(this.detailline, 2);
            this.getNext(this.detailline, 14);
            String whtAttachNo = this.getString(this.detailline, 6);
            String numWHTDetailStr = this.getString(this.detailline, 2);
            String totalWHTAmountStr = this.getAmount(this.detailline, 13, 3);
            String numInvDetailStr = this.getString(this.detailline, 6);
            String totalInvAmountStr = this.getAmount(this.detailline, 13, 3);
            String whtPayType = this.getString(this.detailline, 1);
            String whtRemark = this.getString(this.detailline, 40);
            String whtDate = this.getString(this.detailline, 8);
            String creditBankCode = this.getString(this.detailline, 3);
            this.getNext(this.detailline, 35);
            String creditBranchCode = this.getString(this.detailline, 4);
            this.getNext(this.detailline, 35);
            String whtSignatory = this.getString(this.detailline, 1);
            this.chqNotification = this.getString(this.detailline, 1);
            String chqReferenceNo = this.getString(this.detailline, 20);
            String chqReferenceDocType = this.getString(this.detailline, 1);
            String paymentType = this.getString(this.detailline, 3);
            String serviceType = this.getString(this.detailline, 2);
            this.getNext(this.detailline, 68);
            String beneCharge = this.getString(this.detailline, 2);
            if (!recordType.equals("003")) {
                String msgErr = "There is no credit detail record.";
                this.writeError("Record Type", recordType, 1, 3, msgErr, recordType);
                throw new Exception(this.getLineMsg(this.line) + msgErr);
            }

            ++this.numCredit;
            this.validateNumeric("Credit Sequence Number", recordType, 4, 9, creditSeqNo, 6);
            this.validateCreditAccount("Credit Account", recordType, 10, 34, creditAccountNo, creditBankCode);
            this.validateAmount("Credit Amount", recordType, 35, 50, creditAmountStr, false, false);
            this.validateCreditAmountForPPY(recordType, 35, 50, creditAmountStr);
            this.validateCurrency("Credit Currency", recordType, 51, 53, creditCurrency);
            this.validateInternalReference("Internal Reference", recordType, 54, 61, creditInternalReference);
            this.validateWHTForm(recordType, whtFormType, whtAttachNo, numWHTDetailStr, totalWHTAmountStr, whtPayType, whtRemark, whtDate, whtSignatory);
            this.validateInvoice(recordType, invPresent, numInvDetailStr, totalInvAmountStr);
            this.validateCreditAdvice(recordType, 64, 64, isRequireCrAdvice);
            BigDecimal creditAmount = NumericUtil.parseBigDecimal(creditAmountStr);
            BigDecimal totalWHTAmount = NumericUtil.parseBigDecimal(totalWHTAmountStr);
            this.sumCreditAmount = this.sumCreditAmount.add(creditAmount);
            if (this.whtPresent.equals("Y") || invPresent.equals("Y") || isRequireCrAdvice.equals("Y")) {
                this.validateDeliveryMode(recordType, 65, 65, deliveryMode);
            }

            this.validatePickupLocation(recordType, 66, 69, deliveryMode, pickupLocation);
            this.validateBankCode("Receiving Bank Code", recordType, 181, 183, creditBankCode);
            this.validateBranchCode("Receiving Branch Code", recordType, 219, 222, creditBranchCode);
            this.validateChequeNotification(recordType, 259, 259);
            this.validateChequeReferenceNumber(recordType, 260, 279, chqReferenceNo);
            this.validateChequeReferenceDocumentType(recordType, 280, 280, chqReferenceDocType);
            this.validatePaymentType(recordType, 281, 283, paymentType);
            this.validateServiceType(recordType, 284, 285, serviceType);
            this.validateBeneficiaryCharge(recordType, 354, 355, beneCharge);
            CreditInstructionValue creditInstr = new CreditInstructionValue();
            creditInstr.setSeqNo(NumericUtil.parseInt(creditSeqNo));
            creditInstr.setCustReferenceNo(chqReferenceNo);
            creditInstr.setBankCode(creditBankCode);
            creditInstr.setBranchCode(creditBranchCode);
            creditInstr.setCreditAccountNo(creditAccountNo);
            creditInstr.setBeneCharge(beneCharge);
            creditInstr.setCreditAmount(creditAmount);
            creditInstr.setTotalWHTAmount(totalWHTAmount);
            creditInstr.setTotalInvoiceAmount(NumericUtil.parseBigDecimal(totalInvAmountStr));
            creditInstr.setDeliveryMode(deliveryMode);
            creditInstr.setPickupLocation(pickupLocation);
            this.validatePayeeDetail(_br, creditInstr);

            this.sumWHTAmount = new BigDecimal(0);
            String[] whtIncomeArray = new String[5];
            int countOfIncomeTypeNotSix = 0;
            int numWhtIncomeTypeSix = 0;
            int haveIncomeTypeSix = 0;
            this.mapEWhtAmount005 = new HashMap();
            this.mapEWhtAmount005Line = new HashMap();
            this.mapEWhtAmount006 = new HashMap();
            this.mapEWhtAmount006Line = new HashMap();

            int i;
            String msgErr;
            String key;
            for(i = 0; i < NumericUtil.parseInt(numWHTDetailStr); ++i) {
                this.validateWHTDetail(_br);
                whtIncomeArray[i] = this.whtIncomeType;

                for(int j = i; j > 0; --j) {
                    if (!whtIncomeArray[i].equals("6") && whtIncomeArray[i - j].equals(whtIncomeArray[i])) {
                        msgErr = "Income Tax Type duplicate.";
                        this.writeError("WHT Income Type", "005", 36, 40, msgErr, this.whtIncomeType);
                    }
                }

                if (whtIncomeArray[i].equals("6")) {
                    ++numWhtIncomeTypeSix;
                    if (numWhtIncomeTypeSix > 3) {
                        key = "Income Tax Type 6(Other) should not have more three line.";
                        this.writeError("WHT Income Type", recordType, 36, 40, key, this.whtIncomeType);
                    }

                    haveIncomeTypeSix = 1;
                } else {
                    ++countOfIncomeTypeNotSix;
                }

                if (countOfIncomeTypeNotSix + haveIncomeTypeSix > 3) {
                    key = "Income Tax Type should not have more three type.";
                    this.writeError("WHT Income Type", recordType, 36, 40, key, this.whtIncomeType);
                }
            }

            this.sumWHTAmountOverInv = new BigDecimal(0);

            for(i = 0; i < NumericUtil.parseInt(numInvDetailStr); ++i) {
                this.validateInvoiceDetail(_br);
            }

            if (this.whtPresent.equals("E")) {
                if (totalWHTAmount.compareTo(this.sumWHTAmount) != 0) {
                    msgErr = "Witholding Tax Amount is not equal summary of Witholding Tax Amount(Line 003).";
                    this.writeError("Witholding Tax Amount", recordType, lineforwht, 94, 109, msgErr, this.sumWHTAmount.toString() + "><" + totalWHTAmount.toString());
                }

                if (totalWHTAmount.compareTo(this.sumWHTAmountOverInv) != 0) {
                    msgErr = "Invoice Details Witholding Tax Amount is not equal summary of Witholding Tax Amount(Line 003).";
                    this.writeError("Witholding Tax Amount", recordType, lineforwht, 94, 109, msgErr, this.sumWHTAmountOverInv.toString() + "><" + totalWHTAmount.toString());
                }

                if (mapEWhtAmount005.size() > 0) {
                    for(Map.Entry<String, BigDecimal> entry : mapEWhtAmount005.entrySet()) {
                        key = entry.getKey();
                        if (mapEWhtAmount006.containsKey(key)) {
                            if ((entry.getValue()).compareTo(mapEWhtAmount006.get(key)) != 0) {
                                msgErr = "Witholding Tax Amount of income type " + key + " in line 005 not match with WHT Amount in line 006.";
                                this.writeError("Witholding Tax Amount", "005", mapEWhtAmount005Line.get(key), 20, 35, msgErr, entry.getValue().toString() + "><" + mapEWhtAmount006.get(key).toString());
                            }
                        } else {
                            msgErr = "Witholding Income Type " + key + " not exists in line 006 while exists in line 005.";
                            this.writeError("Witholding Income Type", "005", mapEWhtAmount005Line.get(key), 36, 40, msgErr, key);
                        }
                    }
                }

                if (this.mapEWhtAmount006.size() > 0) {
                    for(Map.Entry<String, BigDecimal> entry : mapEWhtAmount006.entrySet()) {
                        key = entry.getKey();
                        if (!this.mapEWhtAmount005.containsKey(key) && entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                            msgErr = "Witholding Income Type " + key + " not exists in line 005 while exists in line 006.";
                            this.writeError("Witholding Income Type", "006", mapEWhtAmount006Line.get(key), 164, 168, msgErr, key);
                        }
                    }
                }
            }

            _pmtValue.addCreditInstruction(creditInstr);
        }

    }

    private void validatePayeeDetail(BufferedReader _br, CreditInstructionValue _creditInstr) throws Exception {
        this.detailline = this.readLine(_br);
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            String recordType = this.getAndValidateRecordType(this.detailline);
            if (recordType.equals("004")) {
                String internalReference = this.getString(this.detailline, 8);
                String creditSeqNo = this.getString(this.detailline, 6);
                String idCard = this.getString(this.detailline, 15);
                String payeeNameThai = this.getString(this.detailline, 100);
                String address1 = this.getString(this.detailline, 70);
                String address2 = this.getString(this.detailline, 70);
                String address3 = this.getString(this.detailline, 70);
                String taxId = this.getString(this.detailline, 10);
                String payeeNameEng = this.getString(this.detailline, 70);
                String faxNo = this.getString(this.detailline, 10);
                String payee1MobileNo = this.getString(this.detailline, 10);
                log.debug("CustomHashValidation - payee1MobileNo: {}", payee1MobileNo);
                String payee1Email = this.getString(this.detailline, 64);
                this.validateInternalReference("Internal Reference", recordType, 4, 11, internalReference);
                this.validateNumeric("Credit Sequence Number", recordType, 12, 17, creditSeqNo, 6);
                String msgErr;
                if ("BNT".equals(this.productCode)) {
                    if (payeeNameEng.equals("") || !TextUtil.isEnglishName(payeeNameEng)) {
                        msgErr = "Product Code is BNT. Payee1 Name English can not be null or special characters";
                        this.writeError("Payee1 Name(English)", recordType, 353, 422, msgErr, payeeNameEng);
                    }

                    if (address1.equals("") || !TextUtil.isEnglishName(address1)) {
                        msgErr = "Product Code is BNT. Payee1 Addr1 can not be null or special characters";
                        this.writeError("Payee Address 1", recordType, 133, 342, msgErr, address1);
                    }

                    if (!address2.equals("") && !TextUtil.isEnglishName(address2)) {
                        msgErr = "Product Code is BNT. Payee1 Addr2 can not be special characters";
                        this.writeError("Payee Address 2", recordType, 133, 342, msgErr, address2);
                    }

                    if (!address3.equals("") && !TextUtil.isEnglishName(address3)) {
                        msgErr = "Product Code is BNT. Payee1 Addr3 can not be special characters";
                        this.writeError("Payee Address 3", recordType, 133, 342, msgErr, address3);
                    }
                }

                if (DomesticPaymentUtil.isChequeType(this.productCode) && payeeNameThai.equals("") && payeeNameEng.equals("")) {
                    msgErr = "Payee Name is null for Cheque Issuance payment";
                    this.writeError("Payee1 Name Thai and English", recordType, 33, 132, msgErr, payeeNameThai + payeeNameEng);
                }

                if (!this.whtPresent.equals("Y")) {
                    if (this.whtPresent.equals("E")) {
                        this.validateNumeric("Payee1 IDCard", recordType, 18, 32, idCard, 13);
                        this.validateRepeatChar("Payee1 IDCard", recordType, 18, 32, idCard, 13);
                    }
                } else {
                    if ((idCard == null || idCard.equals("")) && (taxId == null || taxId.equals(""))) {
                        msgErr = "Id Card and Tax Id should not space when WHT Present is 'Y'";
                        this.writeError("Payee1 IDCard", recordType, 18, 32, msgErr, idCard);
                    }

                    if ("".equals(idCard)) {
                        msgErr = "WHT Present is 'Y'. Id Card can not be null";
                        this.writeError("Payee1 IDCard", recordType, 18, 32, msgErr, idCard);
                    }
                }

                if (this.chqNotification.equals("F") && (faxNo == null || faxNo.equals("") || !TextUtil.isEnglishName(faxNo))) {
                    msgErr = "Fax Number is require when Beneficiary Notification is F";
                    this.writeError("Payee1 Fax Number", recordType, 423, 432, msgErr, faxNo);
                }

                if (this.chqNotification.equals("E") && (payee1Email == null || payee1Email.equals(""))) {
                    msgErr = "E-mail Address is a required field, Can not found Payee1 E-mail Address";
                    this.writeError("Payee1 E-mail Address", recordType, 443, 506, msgErr, payee1Email);
                }

                String encodedPayeeNameThai = "";
                if(StringUtils.isEmpty(payeeNameThai)) {
                    encodedPayeeNameThai = new String(payeeNameThai.getBytes(StandardCharsets.UTF_8));
                }
                _creditInstr.setPayeeNameThai(encodedPayeeNameThai);
                _creditInstr.setPayeeNameEng(payeeNameEng);
            }
        }

    }

    private void validateWHTDetail(BufferedReader _br) throws Exception {
        this.detailline = this.readLine(_br);
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            String recordType = this.getAndValidateRecordType(this.detailline);
            String internalReference = this.getString(this.detailline, 8);
            String creditSeqNo = this.getString(this.detailline, 6);
            String whtSeqNo = this.getString(this.detailline, 2);
            String whtAmountStr = this.getAmount(this.detailline, 13, 3);
            this.whtIncomeType = this.getString(this.detailline, 5);
            String incomeDescription = this.getString(this.detailline, 77);
            String whtDeductRateStr = this.getString(this.detailline, 5);
            String incomeTypeAmtStr = this.getAmount(this.detailline, 13, 3);
            boolean canAmountZero = true;
            if (!recordType.equals("005")) {
                String msgErr = "There is no WHT detail record.";
                this.writeError("Record Type", recordType, 1, 3, msgErr, recordType);
                throw new Exception(this.getLineMsg(this.line) + msgErr);
            }

            this.validateInternalReference("Internal Reference", recordType, 4, 11, internalReference);
            this.validateNumeric("Credit Sequence Number", recordType, 12, 17, creditSeqNo, 6);
            this.validateNumeric("WHT Sequence Number", recordType, 18, 19, whtSeqNo, 2);
            this.validateAmount("WHT Amount", recordType, 20, 35, whtAmountStr, true, false);
            this.validateWHTIncomeType(recordType, incomeDescription);
            this.validateWHTDeductRate(recordType, 118, 122, whtDeductRateStr);
            BigDecimal whtAmount = NumericUtil.parseBigDecimal(whtAmountStr);
            this.sumWHTAmount = this.sumWHTAmount.add(whtAmount);
            if (this.whtPresent.equals("E")) {
                canAmountZero = false;
                this.mapEWhtAmount005.put(this.whtIncomeType.trim(), whtAmount);
                this.mapEWhtAmount005Line.put(this.whtIncomeType.trim(), this.line);
                if (this.whtIncomeType.equals("099") && whtAmount.compareTo(BigDecimal.ZERO) != 0) {
                    String msgErr = "Witholding Tax Amount of Income Type 099 must be 0.";
                    this.writeError("Witholding Tax Amount", recordType, 20, 35, msgErr, whtAmount.toString());
                }
            }

            this.validateAmount("Income Type Amount", recordType, 123, 138, incomeTypeAmtStr, canAmountZero, false);
        }

    }

    private void validateInvoiceDetail(BufferedReader _br) throws Exception {
        this.detailline = this.readLine(_br);
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            String recordType = this.getAndValidateRecordType(this.detailline);
            String internalReference;
            if (!recordType.equals("006")) {
                internalReference = "There is no invoice detail record.";
                this.writeError("Record Type", recordType, 1, 3, internalReference, recordType);
                throw new Exception(this.getLineMsg(this.line) + internalReference);
            }

            internalReference = this.getString(this.detailline, 8);
            String creditSeqNo = this.getString(this.detailline, 6);
            String invSeqNo = this.getString(this.detailline, 6);
            String invoiceNo = this.getString(this.detailline, 15);
            String invAmountStr = this.getAmount(this.detailline, 13, 3);
            String invoiceDate = this.getString(this.detailline, 8);
            this.getNext(this.detailline, 70);
            this.getNext(this.detailline, 15);
            String vatAmtStr = this.getAmount(this.detailline, 13, 3);
            String whtIncomeType = this.getString(this.detailline, 5);
            this.getNext(this.detailline, 11);
            String whtAmountStr = this.getAmount(this.detailline, 13, 3);
            String printLang = this.getString(this.detailline, 1);
            this.validateInternalReference("Internal Reference", recordType, 4, 11, internalReference);
            this.validateNumeric("Credit Sequence Number", recordType, 12, 17, creditSeqNo, 6);
            this.validateNumeric("Invoice Sequence Number", recordType, 18, 23, invSeqNo, 6);
            this.validateNullorBlank("Invoice Number", recordType, 24, 38, invoiceNo);
            this.validateAmount("Invoice Amount", recordType, 39, 54, invAmountStr, false, true);
            this.validateDate("Invoice Date", recordType, 55, 62, invoiceDate, "yyyyMMdd", false);
            this.validateAmount("VAT Amount", recordType, 148, 163, vatAmtStr, true, true);
            this.validateAmount("WHT Amount", recordType, 180, 195, whtAmountStr, true, true);
            this.validatePrintLanguage(recordType, 196, 196, printLang);
            BigDecimal whtAmount = NumericUtil.parseBigDecimal(whtAmountStr);
            this.sumWHTAmountOverInv = this.sumWHTAmountOverInv.add(whtAmount);
            if (this.whtPresent.equals("E")) {
                if (this.mapEWhtAmount006.containsKey(whtIncomeType)) {
                    this.mapEWhtAmount006.put(whtIncomeType, ((BigDecimal)this.mapEWhtAmount006.get(whtIncomeType)).add(whtAmount));
                } else {
                    this.mapEWhtAmount006.put(whtIncomeType, whtAmount);
                }

                this.mapEWhtAmount006Line.put(whtIncomeType, this.line);
            }
        }

    }

    private void validateTrailer(BufferedReader _br, String _recordType) throws Exception {
        if (this.detailline != null && !this.detailline.trim().equals("")) {
            String totalDebit = this.getString(this.detailline, 6);
            String totalCredit = this.getString(this.detailline, 6);
            String totalAmountStr = this.getAmount(this.detailline, 13, 3);
            String msgErr;
            if (_recordType == null || !_recordType.equals("999")) {
                msgErr = "There is no Trailer record.";
                this.writeError("Record Type", _recordType, 1, 3, msgErr, _recordType);
                throw new Exception(this.getLineMsg(this.line) + msgErr);
            }

            this.validateNumeric("Total No. of Debits", _recordType, 4, 9, totalDebit, 6);
            this.validateNumeric("Total No. of Credits", _recordType, 10, 15, totalCredit, 6);
            if (NumericUtil.parseInt(totalDebit) != this.numDebit) {
                msgErr = "Total No. of Debits miss match with debit records";
                this.writeError("Total No. of Debits", _recordType, 4, 9, msgErr, totalDebit + "><" + NumericUtil.toString(this.numDebit));
            }

            if (NumericUtil.parseInt(totalCredit) != this.numCredit) {
                msgErr = "Total No. of Credits miss match with credits records";
                this.writeError("Total No. of Credits", _recordType, 10, 15, msgErr, totalCredit + "><" + NumericUtil.toString(this.numCredit));
            }

            this.validateAmount("Total Amount", _recordType, 16, 31, totalAmountStr, false, false);
            BigDecimal totalAmount = NumericUtil.parseBigDecimal(totalAmountStr);
            if (totalAmount.compareTo(this.sumAmount) != 0) {
                msgErr = "Total Amount is not equal summary amount of transactions";
                this.writeError("Total Amount", _recordType, 16, 31, msgErr, totalAmountStr);
            }
        }

    }

    private void initialize() {
        this.numDebit = 0;
        this.numCredit = 0;
        this.sumCreditAmount = new BigDecimal(0);
        this.sumWHTAmount = new BigDecimal(0);
        this.sumWHTAmountOverInv = new BigDecimal(0);
        this.sumAmount = new BigDecimal(0);
    }

    private void validateValueDate(String _fieldName, String _recType, int _colStart, int _colEnd, String _value, String _format, boolean _isValidateDateBefore, String _mclCycle) throws Exception {
        this.validateDate(_fieldName, _recType, _colStart, _colEnd, _value, _format, _isValidateDateBefore);
        if (DomesticPaymentUtil.isMediaClearingType(this.productCode) && (_mclCycle.equals("") || _mclCycle.equals("0"))) {
            Date executionDate = DateFormatter.parseDate(_value);
            Date currentDate = DateFormatter.parseDate(DateFormatter.formatDate(DateUtil.getCurrentDate(), _format), _format);
            if (!DateUtil.after(executionDate, currentDate)) {
                String msgErr = _fieldName + " is invalid.";
                this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _value);
            }
        }

    }

    private void validateMCLCycle(String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        if (DomesticPaymentUtil.isMediaClearingType(this.productCode) && !_value.equals("") && !_value.equals("0") && !_value.equals("1") && !_value.equals("2")) {
            String msgErr = "Media Clearing Cycle should be ' ',0,1 or 2.";
            this.writeError("Media Clearing Cycle", _recType, _colStart, _colEnd, msgErr, _value);
        }

    }

    private void validateInternalReference(String _fieldName, String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        if (!_value.equals(this.debitInternalReference)) {
            String msgErr = "Internal Reference is difference value with Debit Detail";
            this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _value);
        }

    }

    private void validateWHTForm(String _recType, String _whtFormType, String _whtAttachNo, String _numWHTDetailStr, String _totalWHTAmountStr, String _whtPayType, String _whtRemark, String _whtDate, String _whtSignatory) throws Exception {
        String msgErr;
        if (this.whtPresent != null && !this.whtPresent.equals("") && (this.whtPresent.equals("Y") || this.whtPresent.equals("N") || this.whtPresent.equals("E"))) {
            if (this.whtPresent.equals("Y")) {
                if (!DomesticPaymentUtil.isValidWHTFormType(_whtFormType)) {
                    msgErr = "WHT Form Type value should be 01 , 02 , 03 , 53 or 54.";
                    this.writeError("WHT Form Type", _recType, 70, 71, msgErr, _whtFormType);
                }

                this.validateNumeric("WHT Attach No.", _recType, 86, 91, _whtAttachNo, 6);
                this.validateNumeric("No. of WHT Details", _recType, 92, 93, _numWHTDetailStr, 2);
                this.validateAmount("Total WHT Amount", _recType, 94, 109, _totalWHTAmountStr, true, false);
                this.validateDate("WHT Deduct Date", _recType, 173, 180, _whtDate, "yyyyMMdd", false);
                if (!DomesticPaymentUtil.isValidWHTPayType(_whtPayType)) {
                    msgErr = "WHT Pay Type value must be 1 , 2 , 3 or 4.";
                    this.writeError("WHT Pay Type", _recType, 132, 132, msgErr, _whtPayType);
                }

                if (NumericUtil.parseInt(_whtPayType) == 4 && _whtRemark.equals("")) {
                    msgErr = "WHT Remark is required when WHT Pay Type value is '4'.";
                    this.writeError("WHT Remark", _recType, 133, 172, msgErr, _whtRemark);
                }

                if (!DomesticPaymentUtil.isValidWHTSignatory(_whtSignatory)) {
                    msgErr = "WHT Signatory value must be B or C.";
                    this.writeError("WHT Signatory", _recType, 258, 258, msgErr, _whtSignatory);
                }
            } else if (this.whtPresent.equals("E")) {
                if (!DomesticPaymentUtil.isValidEWHTFormType(_whtFormType)) {
                    msgErr = "WHT Form Type(E) value should be '11' , '12' , '13' , '14' , '15' , '16' , '17' , '18' or Blank.";
                    this.writeError("WHT Form Type", _recType, 70, 71, msgErr, _whtFormType);
                }

                if (!DomesticPaymentUtil.isValidEWHTPayType(_whtPayType)) {
                    msgErr = "WHT Pay Type(E) value must be 1 , 2 or 3.";
                    this.writeError("WHT Pay Type", _recType, 132, 132, msgErr, _whtPayType);
                }

                if (this.productCode.equals("CCP")) {
                    msgErr = "WHT Present must not be 'E' when product code is " + this.productCode;
                    this.writeError("WHT Present", _recType, 62, 62, msgErr, this.whtPresent);
                }

                this.validateNumeric("WHT Attach No.", _recType, 86, 91, _whtAttachNo, 6);
                this.validateNumeric("No. of WHT Details", _recType, 92, 93, _numWHTDetailStr, 2);
                this.validateAmount("Total WHT Amount", _recType, 94, 109, _totalWHTAmountStr, true, false);
                this.validateDate("WHT Deduct Date", _recType, 173, 180, _whtDate, "yyyyMMdd", false);
            } else {
                this.validateAmount("Total WHT Amount", _recType, 94, 109, _totalWHTAmountStr, true, false);
            }
        } else {
            msgErr = "WHT Present value must be Y , N or E.";
            this.writeError("WHT Present", _recType, 62, 62, msgErr, this.whtPresent);
        }

    }

    private void validateInvoice(String _recType, String _invPresent, String _numInvDetailStr, String _totalInvAmountStr) throws Exception {
        String msgErr;
        if (_invPresent == null || _invPresent.equals("") || !_invPresent.equals("Y") && !_invPresent.equals("N")) {
            msgErr = "Invoice Present value must be Y or N.";
            this.writeError("Invoice Detail Present", _recType, 63, 63, msgErr, _invPresent);
        } else if (_invPresent.equals("Y")) {
            this.validateNumeric("Number of invoice details", _recType, 110, 115, _numInvDetailStr, 6);
            this.validateAmount("Total Invoice Amount", _recType, 116, 131, _totalInvAmountStr, false, false);
        } else {
            if (this.whtPresent.equals("E")) {
                msgErr = "Invoice Present value must be Y when WHT Present is 'E'.";
                this.writeError("Invoice Detail Present", _recType, 63, 63, msgErr, _invPresent);
            }

            this.validateAmount("Total Invoice Amount", _recType, 116, 131, _totalInvAmountStr, true, false);
        }

    }

    private void validateCreditAdvice(String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        if (_value == null || !_value.equals("Y") && !_value.equals("N")) {
            String msgErr = "Credit advice required value must be Y or N.";
            this.writeError("Credit Advice Required", _recType, _colStart, _colEnd, msgErr, _value);
        }

    }

    private void validateDeliveryMode(String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        String msgErr;
        if (DomesticPaymentUtil.isChequeType(this.productCode) && !DomesticPaymentUtil.isValidChequeDeliveryMode(_value)) {
            msgErr = "In product " + this.productCode + " delivery mode should be C M or P";
            this.writeError("Delivery Mode", _recType, _colStart, _colEnd, msgErr, _value);
        }

        if (!DomesticPaymentUtil.isChequeType(this.productCode) && !DomesticPaymentUtil.isValidNonChequeDeliveryMode(_value)) {
            msgErr = "In product " + this.productCode + " delivery mode should be C M P or S";
            this.writeError("Delivery Mode", _recType, _colStart, _colEnd, msgErr, _value);
        }

    }

    private void validatePickupLocation(String _recType, int _colStart, int _colEnd, String _deliveryMode, String _pickupLocation) throws Exception {
        if (_deliveryMode.equals("C") && !this.isValidPickUpLocation(_pickupLocation)) {
            String msgErr = "Invalid pick up location code.";
            this.writeError("Pickup Location", _recType, _colStart, _colEnd, msgErr, _pickupLocation);
        }

    }

    private boolean isValidPickUpLocation(String _value) {
        return pickuplocationlist != null && pickuplocationlist.contains(_value.trim());
    }

    private void validateBankCode(String _fieldName, String _recType, int _colStart, int _colEnd, String _bankCode) throws Exception {
        String msgErr;
        if (!DomesticPaymentUtil.isMediaClearingType(this.productCode) && !this.productCode.equals("BNT")) {
            if (!DomesticPaymentUtil.isPromptPayType(this.productCode) && NumericUtil.parseInt(_bankCode) != Integer.parseInt("14")) {
                msgErr = "This product Bank Code should be 014";
                this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _bankCode);
            }
        } else {
            if (this.productCode.equals("BNT") && "014".equals(_bankCode)) {
                msgErr = "Product Code is BNT. Receiving Bank Code can not be '014'";
                this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _bankCode);
            }

            if (!NumericUtil.isNumeric(_bankCode) || NumericUtil.parseInt(_bankCode) <= 0) {
                msgErr = "Invalid Bank Code";
                this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _bankCode);
            }
        }

    }

    private void validateBranchCode(String _fieldName, String _recType, int _colStart, int _colEnd, String _branchCode) throws Exception {
        if ((DomesticPaymentUtil.isMediaClearingType(this.productCode) || DomesticPaymentUtil.isChequeType(this.productCode) || this.productCode.equals("BNT")) && !NumericUtil.isNumeric(_branchCode)) {
            String msgErr = "Invalid Branch Code";
            this.writeError(_fieldName, _recType, _colStart, _colEnd, msgErr, _branchCode);
        }

    }

    private void validateChequeNotification(String _recType, int _colStart, int _colEnd) throws Exception {
        String msgErr;
        if (DomesticPaymentUtil.isChequeType(this.productCode)) {
            if (this.chqNotification == null || !DomesticPaymentUtil.isValidChqNotification(this.chqNotification)) {
                msgErr = "Beneficiary Notification should be N F E or S.";
                this.writeError("Beneficiary Notification", _recType, _colStart, _colEnd, msgErr, this.chqNotification);
            }
        } else if (this.chqNotification == null || !DomesticPaymentUtil.isValidChqNotification(this.chqNotification) && !this.chqNotification.equals("")) {
            msgErr = "Beneficiary Notification should be N F E S or space.";
            this.writeError("Beneficiary Notification", _recType, _colStart, _colEnd, msgErr, this.chqNotification);
        }

    }

    private void validateChequeReferenceNumber(String _recType, int _colStart, int _colEnd, String _chqRefNo) throws Exception {
        String msgErr;
        if (DomesticPaymentUtil.isChequeType(this.productCode) && (_chqRefNo == null || _chqRefNo.equals(""))) {
            msgErr = "Customer Reference Number is require when product code is " + this.productCode;
            this.writeError("Customer Reference Number", _recType, _colStart, _colEnd, msgErr, _chqRefNo);
        }

        if (this.whtPresent.equals("E") && (_chqRefNo == null || _chqRefNo.equals(""))) {
            msgErr = "Customer Reference Number is require when WHT Present is 'Y'";
            this.writeError("Customer Reference Number", _recType, _colStart, _colEnd, msgErr, _chqRefNo);
        }

    }

    private void validateChequeReferenceDocumentType(String _recType, int _colStart, int _colEnd, String _chqRefDocType) throws Exception {
        if (DomesticPaymentUtil.isChequeType(this.productCode) && !DomesticPaymentUtil.isValidChqRefDocType(_chqRefDocType)) {
            String msgErr = "Cheque Reference Document Type should be 1-9 , A-Z  when product code is " + this.productCode;
            this.writeError("Cheque Reference Document Type", _recType, _colStart, _colEnd, msgErr, _chqRefDocType);
        }

    }

    private void validatePaymentType(String _recType, int _colStart, int _colEnd, String _paymentType) throws Exception {
        String msgErr;
        if (this.productCode.equals("BNT") && !DomesticPaymentUtil.isValidPaymentType(_paymentType)) {
            msgErr = "Invalid Payment Type Code";
            this.writeError("Payment Type Code", _recType, _colStart, _colEnd, msgErr, _paymentType);
        } else if (DomesticPaymentUtil.isPromptPayType(this.productCode) && !DomesticPaymentUtil.isValidPromptPayType(_paymentType)) {
            msgErr = "Invalid PromptPay Type";
            this.writeError("Payment Type Code", _recType, _colStart, _colEnd, msgErr, _paymentType);
        }

    }

    private void validateServiceType(String _recType, int _colStart, int _colEnd, String _svcType) throws Exception {
        String msgErr;
        if (this.productCode.equals("BNT") && !DomesticPaymentUtil.isValidServiceType(_svcType)) {
            msgErr = "Service Type should be 00-23";
            this.writeError("Services Type", _recType, _colStart, _colEnd, msgErr, _svcType);
        } else if (DomesticPaymentUtil.isMediaClearingType(this.productCode) && !DomesticPaymentUtil.isValidMediaClearingServiceType(_svcType)) {
            msgErr = "Service Type should be 01-08 and 59";
            this.writeError("Services Type", _recType, _colStart, _colEnd, msgErr, _svcType);
        }

    }

    private void validateBeneficiaryCharge(String _recType, int _colStart, int _colEnd, String _beneCharge) throws Exception {
        if (!_beneCharge.equals("") && !_beneCharge.equals("B")) {
            String msgErr = "Beneficiary Flag should be 'B '";
            this.writeError("Beneficiary Flag", _recType, _colStart, _colEnd, msgErr, _beneCharge);
        }

    }

    private void validateWHTIncomeType(String _recType, String _incomeDescription) throws Exception {
        String msgErr;
        if (this.whtPresent.equals("E")) {
            if (!DomesticPaymentUtil.isValidEWitholdTaxType(this.whtIncomeType)) {
                msgErr = "Invalid EWHT Income Type";
                this.writeError("WHT Income Type", _recType, 36, 40, msgErr, this.whtIncomeType);
            }
        } else if (!DomesticPaymentUtil.isValidWitholdTaxType(this.whtIncomeType)) {
            msgErr = "Invalid WHT Income Type";
            this.writeError("WHT Income Type", _recType, 36, 40, msgErr, this.whtIncomeType);
        }

        if (this.whtIncomeType.equals("6") && (_incomeDescription == null || _incomeDescription.equals(""))) {
            msgErr = "Income Description is require when WHT Income Type equal 6";
            this.writeError("Income Description", _recType, 41, 117, msgErr, _incomeDescription);
        }

    }

    private void validateWHTDeductRate(String _recType, int _colStart, int _colEnd, String _whtDeductRateStr) throws Exception {
        if (_whtDeductRateStr != null && !_whtDeductRateStr.equals("")) {
            BigDecimal whtDeductRate = NumericUtil.parseBigDecimal(_whtDeductRateStr);
            if (whtDeductRate.compareTo(this.maxdeductrate) >= 0) {
                String msgErr = "WHT Deduct Rate must not over than 99.99";
                this.writeError("WHT Deduct Rate", _recType, _colStart, _colEnd, msgErr, _whtDeductRateStr);
            }
        }

    }

    private void validatePrintLanguage(String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        if (!DomesticPaymentUtil.isValidWHTPrintLanguage(_value)) {
            String msgErr = "Print Language should be E or T";
            this.writeError("Print Language", _recType, _colStart, _colEnd, msgErr, _value);
        }

    }

    private void validateCreditAmountForPPY(String _recType, int _colStart, int _colEnd, String _value) throws Exception {
        BigDecimal cramount = new BigDecimal(_value);
        if (DomesticPaymentUtil.isPromptPayType(this.productCode) && cramount.compareTo(new BigDecimal(2000000)) > 0) {
            String msgErr = "Credit amount must not over than 2,000,000.";
            this.writeError("Credit Amount", _recType, _colStart, _colEnd, msgErr, cramount.toString());
        }

    }
}
