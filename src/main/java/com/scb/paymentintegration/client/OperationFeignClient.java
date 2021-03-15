package com.scb.paymentintegration.client;

import com.scb.paymentintegration.dto.BatchConfigurationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "operationFeignClient", url = "${rest.client.operation-service}")
public interface OperationFeignClient {

	@GetMapping(value = "/ops/batch-processing/config")
    public BatchConfigurationDto getBatchConfiguration();
    
}
