package com.scb.paymentintegration.util;

import com.scb.paymentintegration.constants.ErrorConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class PropertyUtilsTest {

    @Mock
    MessageSource messageSource;

    @InjectMocks
    PropertyUtils propertyUtils;

    @BeforeAll
    static void setup() {

    }

    @Test
    void testPropertyProperties() {
        String key = ErrorConstants.INVALID_DATA_EX_MSG;
        Mockito.when(messageSource.getMessage(key, null, Locale.getDefault())).thenReturn("This is my custom property");
        assertNotNull(propertyUtils.getProperty(key));
    }
}