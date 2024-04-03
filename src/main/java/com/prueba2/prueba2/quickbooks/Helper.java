package com.prueba2.prueba2.quickbooks;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class Helper {

    @Inject
    Credentials credentials;

    public DataService getDataService(String realmId, String accessToken) throws FMSException {
        Context context = prepareContext(realmId, accessToken);
        // create dataservice
        return new DataService(context);
    }

    private Context prepareContext(String realmId, String accessToken) throws FMSException {
        String url = credentials.getIntuitAccountingAPIHost() + "/v3/company";

        Config.setProperty(Config.BASE_URL_QBO, url);
        //create oauth object
        OAuth2Authorizer oauth = new OAuth2Authorizer(accessToken);
        //create context
        Context context = new Context(oauth, ServiceType.QBO, realmId);
        return context;
    }

}
