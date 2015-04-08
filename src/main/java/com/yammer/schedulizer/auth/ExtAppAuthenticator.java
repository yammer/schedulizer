package com.yammer.schedulizer.auth;

import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.entities.Employee;

public abstract class ExtAppAuthenticator {
    protected final Client client;

    public ExtAppAuthenticator(Client client) {
        this.client = client;
    }

    // Return null means unauthorized
    public abstract Employee getTokenOwner(String accessToken);
}
