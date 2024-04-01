package com.prueba2.prueba2.quickbooks;

import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.exception.InvalidRequestException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Named
@RequestScoped
public class ConnectController {

    private OAuth2Config oauth2Config;

    public void connectToQuickBooks() {
        try {
            Properties prop = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(input);
            String clientId = prop.getProperty("OAuth2AppClientId");
            String clientSecret = prop.getProperty("OAuth2AppClientSecret");
            String redirectUri = prop.getProperty("OAuth2AppRedirectUri");

            //initialize the config
            oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(clientId, clientSecret)
                    .callDiscoveryAPI(Environment.SANDBOX)
                    .buildConfig();

            //generate csrf token
            String csrf = oauth2Config.generateCSRFToken();

            //prepare scopes
            List<Scope> scopes = new ArrayList<Scope>();
            scopes.add(Scope.Accounting);

            //prepare authorization url to intiate the oauth handshake
            String authorizationUrl = oauth2Config.prepareUrl(scopes, redirectUri, csrf);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect(authorizationUrl);

        } catch (IOException | InvalidRequestException ex) {
            ex.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to connect to QuickBooks: " + ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

}
