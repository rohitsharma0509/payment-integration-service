package com.scb.paymentintegration.client.handler;

import com.scb.paymentintegration.client.OperationFeignClient;
import com.scb.paymentintegration.dto.BatchConfigurationDto;
import com.scb.paymentintegration.exception.ExternalServiceInvocationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationServiceClient {

    @Autowired
    private OperationFeignClient operationFeignClient;

    public BatchConfigurationDto getBatchConfiguration() {
        try {
            return operationFeignClient.getBatchConfiguration();
        } catch (Exception e) {
            log.info("Exception while connecting with operation service", e);
            throw new ExternalServiceInvocationException("Either operation-service is not available or data not configured.");
        }
    }

}
