package com.scb.paymentintegration.util;

import com.scb.paymentintegration.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

@Slf4j
public class S1FileUtils {

    private static final String S1_AMOUNT_FORMAT = "0000000000000.000";

    private S1FileUtils() {
    }

    public static String getS1FileName(String scbCorpId, String extension) {
        StringBuilder sb = new StringBuilder("BulkERP_");
        sb.append(scbCorpId);
        sb.append("014");
        sb.append("PAY");
        sb.append(CommonUtils.getFormattedCurrentDateTime(Constants.DATETIME_FORMAT_YYYYMMDDHHMMSS));
        sb.append(extension);
        return sb.toString();
    }

    public static String getStringWithSpace(int length) {
        return StringUtils.rightPad(StringUtils.EMPTY, length, Constants.BLANK_CHAR);
    }

    public static String toS1FormattedString(String str, int length) {
        if (str.length() > length) {
            return str.substring(0, length);
        } else if (str.length() < length) {
            return StringUtils.rightPad(str, length, Constants.BLANK_CHAR);
        }
        return str;
    }

    public static String toS1FormattedAmount(Double d) {
        log.info("Converting double to s1 formatted amount (v13v3): {}", d);
        DecimalFormat df = new DecimalFormat(S1_AMOUNT_FORMAT);
        return df.format(d).replace(".", StringUtils.EMPTY);
    }

    public static Double toFormattedDouble(String amount) {
        log.info("Converting amount (v13v3) to double: {}", amount);
        String beforeDecimalStr = amount.substring(0, 13);
        String afterDecimalStr = amount.substring(13, 16);
        String doubleStr = beforeDecimalStr.concat(".").concat(afterDecimalStr);
        return Double.parseDouble(doubleStr);
    }
    public static Double toFormattedDoubleV14V4(String amount) {
        log.info("Converting amount (V14V4) to double: {}", amount);
        String beforeDecimalStr = amount.substring(0, 14);
        String afterDecimalStr = amount.substring(14, 18);
        String doubleStr = beforeDecimalStr.concat(".").concat(afterDecimalStr);
        return Double.parseDouble(doubleStr);
    }
}
