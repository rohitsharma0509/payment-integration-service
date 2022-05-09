package com.scb.paymentintegration.constants;

import java.time.ZoneId;

public class Constants {

    private Constants() {}

    public static final Character BLANK_CHAR = ' ';
    public static final String TIME_FORMAT_HHMM = "HHmm";
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
    public static final String DATETIME_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final ZoneId BKK_ZONE_ID = ZoneId.of("Asia/Bangkok");
}
