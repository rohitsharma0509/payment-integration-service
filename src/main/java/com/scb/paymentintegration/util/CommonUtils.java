package com.scb.paymentintegration.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CommonUtils {
    private CommonUtils() {}

    public static String getFormattedCurrentDate(String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(LocalDate.now());
    }

    public static String getFormattedCurrentTime(String format) {
        DateTimeFormatter dft = DateTimeFormatter.ofPattern(format);
        return dft.format(LocalTime.now());
    }

    public static String getFormattedCurrentDateTime(String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(LocalDateTime.now());
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }

    public static String sanitize(byte[] strBytes) {
        return new String(strBytes)
                .replace("\r", "")
                .replace("\n", "");
    }

}
