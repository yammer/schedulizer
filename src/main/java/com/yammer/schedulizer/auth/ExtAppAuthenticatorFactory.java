package com.yammer.schedulizer.auth;

import com.sun.jersey.api.client.Client;

public class ExtAppAuthenticatorFactory {
    public enum ExtAppType {
        yammer, facebook
    }
    public static ExtAppAuthenticator getExtAppAuthenticator(String type, Client client) {
        switch(ExtAppType.valueOf(type)) {
            case yammer:
                return new YammerAuthenticator(client);
            case facebook:
                return new FacebookAuthenticator(client);
        }
        return null;
    }
}
