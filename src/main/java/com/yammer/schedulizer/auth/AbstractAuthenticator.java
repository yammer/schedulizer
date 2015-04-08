package com.yammer.schedulizer.auth;


import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.UserManager;

public abstract class AbstractAuthenticator implements io.dropwizard.auth.Authenticator<Credentials, User> {

    protected final Client client;
    protected final UserManager userManager;
    protected final EmployeeManager employeeManager;
    protected final ExtAppAuthenticator extAppAuthenticator;
    protected final ExtAppType extAppType;

    public AbstractAuthenticator(Client client, UserManager userManager,
                                 EmployeeManager employeeManager,
                                 ExtAppAuthenticator extAppAuthenticator,
                                 ExtAppType extAppType) {
        this.client = client;
        this.userManager = userManager;
        this.employeeManager = employeeManager;
        this.extAppAuthenticator = extAppAuthenticator;
        this.extAppType = extAppType;
    }
}
