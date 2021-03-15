package com.scb.paymentintegration.client.handler;

import com.scb.paymentintegration.client.OperationFeignClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.exception.ExternalServiceInvocationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceClientTest {

    private static final String COMPANY_ID = "RBH0001";

    @InjectMocks
    private OperationServiceClient operationServiceClient;

    @Mock
    private OperationFeignClient operationFeignClient;

    @Test
    void throwExceptionGetBatchConfiguration() {
        when(operationFeignClient.getBatchConfiguration()).thenThrow(new NullPointerException());
        assertThrows(ExternalServiceInvocationException.class, () -> operationServiceClient.getBatchConfiguration());
    }

    @Test
    void shouldGetBatchConfiguration() {
        BatchConfigurationDto config = BatchConfigurationDto.builder().companyId(COMPANY_ID).build();
        when(operationFeignClient.getBatchConfiguration()).thenReturn(config);
        BatchConfigurationDto result = operationServiceClient.getBatchConfiguration();
        assertEquals(COMPANY_ID, result.getCompanyId());
    }
}
