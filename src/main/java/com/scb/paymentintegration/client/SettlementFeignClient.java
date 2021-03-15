package com.scb.paymentintegration.client;

import com.scb.paymentintegration.dto.ReturnFileResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "settlementFeignClient", url = "${rest.client.settlement-service}")
public interface SettlementFeignClient {

    @PostMapping(value = "/api/settlement/batch")
    public Boolean saveReturnFileResponse(@RequestBody ReturnFileResult result);
}
