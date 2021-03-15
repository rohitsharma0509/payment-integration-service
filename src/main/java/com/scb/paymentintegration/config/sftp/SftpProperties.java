package com.scb.paymentintegration.config.sftp;

import com.scb.paymentintegration.util.CommonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
@Component
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "sftp.client")
public class SftpProperties {

    @Value("${secretsPath}")
    private String secretsPath;

    private String host;
    private Integer port;
    private String protocol;
    private String username;
    @Setter(AccessLevel.NONE)
    private String password;
    private String root;
    private String privateKey;
    private String passphrase;
    private String sessionStrictHostKeyChecking;
    private Integer sessionConnectTimeout;
    private Integer channelConnectedTimeout;

    @SneakyThrows
    @PostConstruct
    public void setSftpPassword() {
        URI mongoUriPath = ResourceUtils.getURL(secretsPath + "/S1_SFTP_PASSWORD").toURI();
        String pswrd = CommonUtils.sanitize(Files.readAllBytes(Paths.get(mongoUriPath)));
        this.password = pswrd;
    }
}
