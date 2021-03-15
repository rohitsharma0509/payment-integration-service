package com.scb.paymentintegration.bdd.stepdefinitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.paymentintegration.dto.RiderSettlementDetails;
import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.dto.SftpRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
public class FileHandlerAndSftpControllerStepDefinitions {

    private MvcResult result;
    private static String POST_URL = "";
    private static String S3_URL;
    private static final String BATCH_REF_NO = "S1TESTCASES01";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Given("Set POST Generate s1 file service api endpoint")
    public void set_post_generate_s1_file_service_api_endpoint() {
        POST_URL = "/api/generate/s1";
    }

    @When("Send a POST HTTP request for Generate s1 file")
    public void send_a_post_http_request_for_generate_s1_file() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderSettlementRequest());
        log.info("Running functional tests for generating s1 file, endpoint: {}", POST_URL);
        result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
        S3_URL = result.getResponse().getContentAsString();
    }

    @Then("I receive valid HTTP Response Code {int} for Generate s1 file")
    public void i_receive_valid_http_response_code_for_generate_s1_file(Integer statusCode) throws UnsupportedEncodingException {
        log.info("Result of functional tests: statusCode: {}, S3_URL: {}", statusCode, S3_URL);
        assertEquals(HttpStatus.CREATED.value(), statusCode, "Incorrect Response Status");
        assertNotNull(S3_URL);
    }

    @Given("Set POST Upload S1 file to sftp service api endpoint")
    public void set_post_upload_s1_file_to_sftp_service_api_endpoint() {
        POST_URL = "/api/sftp/upload";
    }

    @When("Send a POST HTTP request for Upload S1 file to sftp")
    public void send_a_post_http_request_for_upload_s1_file_to_sftp() throws Exception {
        log.info("Running functional tests for upload s1 file to sftp, endpoint: {}", POST_URL);
        SftpRequest request = SftpRequest.builder().fileName(S3_URL).batchReferenceNumber(BATCH_REF_NO).build();
        String json = objectMapper.writeValueAsString(request);
        result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} for Upload S1 file to sftp")
    public void i_receive_valid_http_response_code_for_upload_s1_file_to_sftp(Integer statusCode) throws UnsupportedEncodingException {
        assertEquals(HttpStatus.OK.value(), statusCode, "Incorrect Response Status");
        assertTrue(Boolean.parseBoolean(result.getResponse().getContentAsString()));
    }

    private RiderSettlementRequest getRiderSettlementRequest() {
        RiderSettlementDetails riderDetails1 = RiderSettlementDetails.builder()
                .riderId("RRTEST1").riderName("Test Rider1").riderAccountNumber("1115168927").totalCreditAmount(660.0).build();
        RiderSettlementDetails riderDetails2 = RiderSettlementDetails.builder()
                .riderId("RRTEST2").riderName("Test Rider2").riderAccountNumber("4680378283").totalCreditAmount(380.0).build();
        return RiderSettlementRequest.builder()
                .batchReferenceNumber(BATCH_REF_NO).totalDebitAmount(1040.0).totalConsolidatedAmount(1040.0)
                .noOfCredits(2).riderSettlementDetails(Arrays.asList(riderDetails1, riderDetails2))
                .build();
    }

}
