package com.scb.paymentintegration.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.paymentintegration.dto.RiderSettlementDetails;
import com.scb.paymentintegration.dto.RiderSettlementRequest;
import java.util.Arrays;
import java.util.List;

import com.scb.paymentintegration.dto.SftpRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileHandlerAndSftpControllerIntegrationTest {

    private static final String GENERATE_S1_ENDPOINT = "/api/generate/s1";
    private static final String UPLOAD_TO_SFTP_ENDPOINT = "/api/sftp/upload";
    private static final String BATCH_REF_NO = "S1TESTCASES01";
    private static String fileName;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void shouldGenerateAndUploadS1FileToS3() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderSettlementRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_S1_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        fileName = result.getResponse().getContentAsString();
        assertNotNull(fileName);
    }

    @Test
    @Order(2)
    void shouldUploadS1FileToSftp() throws Exception {
        SftpRequest request = SftpRequest.builder().batchReferenceNumber(BATCH_REF_NO).fileName(fileName).build();
        String json = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(UPLOAD_TO_SFTP_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
    }

    @Test
    @Order(3)
    void shouldNotGenerateAndUploadS1FileWhenAmountIsInvalidAndReturn400() throws Exception {
        RiderSettlementRequest request = getRiderSettlementRequest();
        request.setTotalConsolidatedAmount(1050.0);
        request.setTotalDebitAmount(1050.0);
        String json = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_S1_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    }

    @Test
    @Order(4)
    void shouldNotGenerateAndUploadS1FileWhenAccountNumberIsInvalidAndReturn400() throws Exception {
        RiderSettlementRequest request = getRiderSettlementRequest();
        List<RiderSettlementDetails> riderDetails = request.getRiderSettlementDetails();
        riderDetails.get(0).setRiderAccountNumber("1234567890");
        String json = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_S1_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
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

