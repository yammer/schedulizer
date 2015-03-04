package com.yammer.stresstime.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.UserManager;
import com.yammer.stresstime.utils.CoreUtils;
import io.dropwizard.auth.AuthenticationException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

public class MockAuthenticator extends Authenticator {

    public MockAuthenticator(Client client, UserManager userManager, EmployeeManager employeeManager) {
        super(client, userManager, employeeManager);
    }

    @Override
    protected Employee getTokenOwner(Credentials credentials) throws AuthenticationException {
        return employeeManager.safeGetByYammerId(credentials.getYammerId());
    }
}
