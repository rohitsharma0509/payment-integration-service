package com.scb.paymentintegration.controller;

import com.scb.paymentintegration.dto.RiderSettlementRequest;
import com.scb.paymentintegration.service.FileHandlerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileHandlerController {

    @Autowired
    private FileHandlerService fileHandlerService;

    @PostMapping("/generate/s1")
    @ApiOperation(nickname = "generate-and-upload-s1-file", value = "Generate s1 file and upload it to s3", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(response = String.class, code = 201, message = "S1 file generated and uploaded successfully"),
            @ApiResponse(code = 400, message = "Bad request")})
    @Valid
    public ResponseEntity<String> generateAndUploadS1File(@RequestBody @Valid @NotEmpty final @NotNull RiderSettlementRequest riderSettlementRequest) throws IOException {
        String s3Url = fileHandlerService.generateAndUploadS1File(riderSettlementRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(s3Url);
    }
}
