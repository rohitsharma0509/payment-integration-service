package com.scb.paymentintegration.service;

import java.io.File;
import java.io.InputStream;

public interface CryptoService {
    File encrypt(File s1HashFile, String publicKey, String encryptedFileName, boolean withIntegrityCheck);
    InputStream decrypt(InputStream encryptedFileStream, String keyFileName, char[] passwd);
}
