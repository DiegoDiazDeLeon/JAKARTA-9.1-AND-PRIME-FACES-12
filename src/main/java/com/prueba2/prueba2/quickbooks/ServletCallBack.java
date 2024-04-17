package com.prueba2.prueba2.quickbooks;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebServlet("/oauth2redirect")
public class ServletCallBack extends HttpServlet {

    private String authCode;
    private String state;
    private String realmId;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            authCode = request.getParameter("code");
            state = request.getParameter("state");
            realmId = request.getParameter("realmId");

            Properties prop = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(input);
            String clientId = prop.getProperty("OAuth2AppClientId");
            String clientSecret = prop.getProperty("OAuth2AppClientSecret");
            String redirectUri = prop.getProperty("OAuth2AppRedirectUri");

            OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(clientId, clientSecret)
                    .callDiscoveryAPI(Environment.SANDBOX)
                    .buildConfig();

            OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);
            BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

            // Almacenar los tokens en la sesión
            request.getSession().setAttribute("realmId",realmId);
            request.getSession().setAttribute("code",authCode);
            request.getSession().setAttribute("access_token", bearerTokenResponse.getAccessToken());
            request.getSession().setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());

            // Redirigir a la página de conexión exitosa o a otra página según sea necesario
            response.sendRedirect(request.getContextPath() + "/connectOK.xhtml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (
                OAuthException e) {
            throw new RuntimeException(e);
        }

    }

}
