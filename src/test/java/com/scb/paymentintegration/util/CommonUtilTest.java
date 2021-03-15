package com.scb.paymentintegration.util;


import com.scb.paymentintegration.constants.Constants;
import com.scb.paymentintegration.enums.FileType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonUtilTest {

    private static final String RET_FILE_NAME = "RBH0001_20210127_DCP_200.txt.gpg";
    private static final String CTRL_FILE_NAME = "RBH0001_20210127_DCP_200.ctrl";

    @Test
    void shouldGetFormattedCurrentDate() {
        String result = CommonUtils.getFormattedCurrentDate(Constants.DATE_FORMAT_YYYYMMDD);
        Assertions.assertNotNull(result);
    }

    @Test
    void shouldGetFormattedCurrentTime() {
        String result = CommonUtils.getFormattedCurrentTime(Constants.TIME_FORMAT_HHMM);
        Assertions.assertNotNull(result);
    }

    @Test
    void shouldGetFormattedCurrentDateTime() {
        String result = CommonUtils.getFormattedCurrentDateTime(Constants.DATETIME_FORMAT_YYYYMMDDHHMMSS);
        Assertions.assertNotNull(result);
    }

    @Test
    void shouldReplaceLast() {
        String result = CommonUtils.replaceLast(RET_FILE_NAME, FileType.TXT_GPG.getExtension(), FileType.CTRL.getExtension());
        Assertions.assertEquals(CTRL_FILE_NAME, result);
    }
}
