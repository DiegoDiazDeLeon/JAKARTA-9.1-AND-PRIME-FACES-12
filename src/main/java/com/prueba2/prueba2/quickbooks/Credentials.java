package com.prueba2.prueba2.quickbooks;

import jakarta.inject.Named;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Named
public class Credentials {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String intuitAccountingAPIHost;

    @PostConstruct
    public void init() {
        Properties prop = new Properties();
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clientId = prop.getProperty("OAuth2AppClientId");
        setClientId(clientId);
        clientSecret = prop.getProperty("OAuth2AppClientSecret");
        setClientSecret(clientSecret);
        redirectUri = prop.getProperty("OAuth2AppRedirectUri");
        setRedirectUri(redirectUri);
        intuitAccountingAPIHost = prop.getProperty("IntuitAccountingAPIHost");
        setIntuitAccountingAPIHost(intuitAccountingAPIHost);
    }

    public String getIntuitAccountingAPIHost() {
        return intuitAccountingAPIHost;
    }

    public void setIntuitAccountingAPIHost(String intuitAccountingAPIHost) {
        this.intuitAccountingAPIHost = intuitAccountingAPIHost;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
