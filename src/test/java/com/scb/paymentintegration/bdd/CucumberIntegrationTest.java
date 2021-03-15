package com.scb.paymentintegration.bdd;

import com.scb.paymentintegration.RiderPaymentIntegrationApplication;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(Cucumber.class)
@CucumberContextConfiguration
@SpringBootTest(classes = {
        RiderPaymentIntegrationApplication.class,
        CucumberIntegrationTest.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@CucumberOptions(plugin = {"pretty"}, tags = "not @ignore",features = "src/test/resources/scenarios")
@AutoConfigureMockMvc
public class CucumberIntegrationTest {

    @Test
    public void test(){
        Assertions.assertEquals(true, Boolean.TRUE);
    }
}
