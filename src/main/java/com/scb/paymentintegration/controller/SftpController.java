package com.scb.paymentintegration.controller;

import com.scb.paymentintegration.dto.SftpRequest;
import com.scb.paymentintegration.service.SftpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/sftp")
public class SftpController {

    @Autowired
    private SftpService sftpService;

    @PostMapping("/upload")
    @ApiOperation(nickname = "upload-file-to-sftp", value = "upload a file to sftp")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "file uploaded successfully."),
            @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Boolean> uploadFileToSftp(@RequestBody SftpRequest sftpRequest) throws IOException {
        boolean isUploaded = sftpService.uploadFile(sftpRequest);
        return ResponseEntity.ok(isUploaded);
    }

    @GetMapping("/poll")
    @ApiOperation(nickname = "poll-sftp-output-path", value = "poll sftp output path and process return file")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "file downloaded successfully."),
            @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Boolean> pollSftp() {
        boolean isSuccess = sftpService.pollOutputPath();
        return ResponseEntity.ok(isSuccess);
    }
}
