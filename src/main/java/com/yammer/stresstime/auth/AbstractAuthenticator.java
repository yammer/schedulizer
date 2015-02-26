package com.yammer.stresstime.auth;


import com.sun.jersey.api.client.Client;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.UserManager;

public abstract class AbstractAuthenticator implements io.dropwizard.auth.Authenticator<Credentials, User> {

    protected final Client client;
    protected final UserManager userManager;
    protected final EmployeeManager employeeManager;

    public AbstractAuthenticator(Client client, UserManager userManager, EmployeeManager employeeManager) {
        this.client = client;
        this.userManager = userManager;
        this.employeeManager = employeeManager;
    }
}
