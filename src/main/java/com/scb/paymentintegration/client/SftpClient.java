package com.scb.paymentintegration.client;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.scb.paymentintegration.config.sftp.SftpProperties;
import com.scb.paymentintegration.exception.ExternalServiceInvocationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class SftpClient {

    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    @Autowired
    private SftpProperties sftpProperties;

    public ChannelSftp connect() {
        JSch jsch = new JSch();
        Channel channel = null;

        try {
            if (!StringUtils.isEmpty(sftpProperties.getPrivateKey())) {
                if (StringUtils.isEmpty(sftpProperties.getPassphrase())) {
                    jsch.addIdentity(sftpProperties.getPrivateKey(), sftpProperties.getPassphrase());
                } else {
                    jsch.addIdentity(sftpProperties.getPrivateKey());
                }
            }
            log.info("Try to connect sftp[" + sftpProperties.getUsername() + "@" + sftpProperties.getHost() + "], use private key[" + sftpProperties.getPrivateKey() +"]");

            Session session = jsch.getSession(sftpProperties.getUsername(), sftpProperties.getHost(), sftpProperties.getPort());
            session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, sftpProperties.getSessionStrictHostKeyChecking());
            session.setPassword(sftpProperties.getPassword());
            session.connect(sftpProperties.getSessionConnectTimeout());
            log.info("Session connected to " + sftpProperties.getHost() + ".");

            // Create sftp communication channel
            channel = session.openChannel(sftpProperties.getProtocol());
            channel.connect(sftpProperties.getChannelConnectedTimeout());
            log.info("Channel created to " + sftpProperties.getHost() + ".");
        } catch (JSchException e) {
            throw new ExternalServiceInvocationException("Unable to connect with sftp");
        }
        return (ChannelSftp) channel;
    }

    public void disconnect(ChannelSftp channelSftp) {
        try {
            if (channelSftp == null)
                return;

            if (channelSftp.isConnected())
                channelSftp.disconnect();

            if (channelSftp.getSession() != null)
                channelSftp.getSession().disconnect();

        } catch (Exception ex) {
            log.error("SFTP disconnect error", ex);
        }
    }
}
