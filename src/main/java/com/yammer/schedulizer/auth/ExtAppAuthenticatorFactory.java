package com.yammer.schedulizer.auth;

import com.sun.jersey.api.client.Client;

public class ExtAppAuthenticatorFactory {
    public static ExtAppAuthenticator getExtAppAuthenticator(ExtAppType type, Client client) {
        switch(type) {
            case yammer:
                return new YammerAuthenticator(client);
            case facebook:
                return new FacebookAuthenticator(client);
        }
        return null;
    }
}
