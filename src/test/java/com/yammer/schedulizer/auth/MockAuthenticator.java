package com.yammer.schedulizer.auth;

import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.UserManager;
import io.dropwizard.auth.AuthenticationException;

public class MockAuthenticator extends Authenticator {

    public MockAuthenticator(Client client, UserManager userManager, EmployeeManager employeeManager) {
        super(client, userManager, employeeManager);
    }

    @Override
    protected Employee getTokenOwner(Credentials credentials) throws AuthenticationException {
        return employeeManager.safeGetByYammerId(credentials.getYammerId());
    }
}
