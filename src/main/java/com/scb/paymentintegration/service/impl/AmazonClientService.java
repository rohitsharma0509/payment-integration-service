package com.scb.paymentintegration.service.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import javax.annotation.PostConstruct;

public class AmazonClientService {

  // AmazonS3 Client, in this object you have all AWS API calls about S3.
  private AmazonS3 s3Client;

  // Getters for parents.
  protected AmazonS3 getClient() {
    return s3Client;
  }

  // This method are called after Spring starts AmazonClientService into your container.
  @PostConstruct
  private void init() {
    Regions clientRegion = Regions.AP_SOUTHEAST_1;
    this.s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();
  }
}
