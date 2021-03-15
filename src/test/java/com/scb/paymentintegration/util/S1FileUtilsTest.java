package com.scb.paymentintegration.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S1FileUtilsTest {
    private static final String COMPANY_ID = "temp1234";
    private static final String EXTENSION = ".txt";
    private static final String TEMP_STR = "123";
    private static final double AMOUNT = 10.0;
    private static final String S1_FORMATTED_AMT = "0000000000010000";
    private static final String S1_FORMATTED_AMT_V14V4 = "000000000000100000";

    @Test
    void shouldGetS1FileName() {
        String result = S1FileUtils.getS1FileName(COMPANY_ID, EXTENSION);
        Assertions.assertEquals(40, result.length());
    }

    @Test
    void shouldToS1FormattedStringWhenLengthIsShort() {
        String result = S1FileUtils.toS1FormattedString(TEMP_STR, 5);
        Assertions.assertEquals("123  ", result);
    }

    @Test
    void shouldToS1FormattedStringWhenLengthIsLong() {
        String result = S1FileUtils.toS1FormattedString(TEMP_STR, 2);
        Assertions.assertEquals("12", result);
    }

    @Test
    void shouldToS1FormattedStringWhenLengthIsEquals() {
        String result = S1FileUtils.toS1FormattedString(TEMP_STR, 3);
        Assertions.assertEquals(TEMP_STR, result);
    }

    @Test
    void shouldGetStringWithSpace() {
        String result = S1FileUtils.getStringWithSpace(2);
        Assertions.assertEquals("  ", result);
    }

    @Test
    void shouldToS1FormattedAmount() {
        String result = S1FileUtils.toS1FormattedAmount(AMOUNT);
        Assertions.assertEquals(S1_FORMATTED_AMT, result);
    }

    @Test
    void shouldToFormattedDouble() {
        Double result = S1FileUtils.toFormattedDouble(S1_FORMATTED_AMT);
        Assertions.assertEquals(AMOUNT, result);
    }

    @Test
    void shouldToFormattedDoubleV14V4() {
        Double result = S1FileUtils.toFormattedDoubleV14V4(S1_FORMATTED_AMT_V14V4);
        Assertions.assertEquals(AMOUNT, result);
    }
}
