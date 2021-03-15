package com.scb.paymentintegration.enums;

public enum FileType {
    GPG(".gpg"),
    CTRL(".ctrl"),
    TXT(".txt"),
    TXT_GPG(".txt.gpg");

    private String extension;

    private FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
