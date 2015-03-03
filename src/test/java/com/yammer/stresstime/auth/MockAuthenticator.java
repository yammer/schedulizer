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

public class MockAuthenticator extends AbstractAuthenticator {

    public MockAuthenticator(Client client, UserManager userManager, EmployeeManager employeeManager) {
        super(client, userManager, employeeManager);
    }

    @Override
    public Optional<User> authenticate(Credentials credentials) throws AuthenticationException {
        User user;
        if (!credentials.isPresent()) {
            user = User.guest();
        } else {
            String yammerId = credentials.getYammerId();
            Employee employee = employeeManager.safeGetByYammerId(yammerId);
            if (employee == null) {
                user = User.guest();
            } else {
                user = employee.getUser();
                if (user == null) {
                    user = new User(employee, credentials.getAccessToken());
                } else if (!credentials.getAccessToken().equals(user.getAccessToken())) {
                    user.setAccessToken(credentials.getAccessToken());
                    user.expire();
                }
            }
        }
        boolean authorized = credentials.getAcceptedRoles().contains(user.getRole());
        return (authorized) ? Optional.of(user) : Optional.absent();
    }
}
