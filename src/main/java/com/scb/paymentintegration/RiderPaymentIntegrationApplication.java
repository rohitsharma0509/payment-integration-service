package com.scb.paymentintegration;

import com.scb.rider.tracing.tracer.EnableBasicTracer;
import com.scb.rider.tracing.tracer.logrequest.EnableRequestLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableRequestLog
@EnableBasicTracer
public class RiderPaymentIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiderPaymentIntegrationApplication.class, args);
	}

}
