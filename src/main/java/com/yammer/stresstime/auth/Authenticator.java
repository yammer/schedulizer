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

public class Authenticator implements io.dropwizard.auth.Authenticator<Credentials, User> {

    private static final String YAMMER_CURRENT_USER_ENDPOINT = "https://www.yammer.com/api/v1/users/current.json";
    private static final String YAMMER_AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    private static final int YAMMER_REQUEST_RETRIES = 3;

    private final Client client;
    private final UserManager userManager;
    private final EmployeeManager employeeManager;

    public Authenticator(Client client, UserManager userManager, EmployeeManager employeeManager) {
        this.client = client;
        this.userManager = userManager;
        this.employeeManager = employeeManager;
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
                employee = getTokenOwner(credentials.getAccessToken());
                if (employee == null || !employee.getYammerId().equals(yammerId)) {
                    return Optional.absent();
                }
                // User verified successfully
                employeeManager.save(employee);
                user = User.fresh(employee, credentials.getAccessToken());
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
        return authenticate(user, credentials.getAcceptedRoles());
    }

    // Don't save the user if cannot verify
    private Optional<User> authenticate(User user, Set<Role> acceptedRoles) throws AuthenticationException {
        if (!user.isGuest()) {
            if (!user.isUpToDate()) {
                Employee tokenOwner = getTokenOwner(user.getAccessToken());
                if (tokenOwner != null && tokenOwner.getYammerId().equals(user.getEmployee().getYammerId())) {
                    user.renew();
                } else {
                    return Optional.absent();
                }
            }
            userManager.save(user);
        }
        boolean authorized = acceptedRoles.contains(user.getRole());
        return (authorized) ? Optional.of(user) : Optional.absent();
    }

    // Return null means unauthorized
    private Employee getTokenOwner(String accessToken) throws AuthenticationException {
        Exception last = null;
        for (int i = 0; i < YAMMER_REQUEST_RETRIES; i++) {
            try {
                return getTokenOwnerOnce(accessToken);
            } catch (Exception exception) {
                Optional<UniformInterfaceException> cause =
                        CoreUtils.getCause(exception, UniformInterfaceException.class);
                if (cause.isPresent() &&
                        cause.get().getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                    return null;
                }
                last = exception;
                exception.printStackTrace();
            }
        }
        throw new AuthenticationException(last);
    }

    private Employee getTokenOwnerOnce(String accessToken) {
        JsonNode response = client.resource(YAMMER_CURRENT_USER_ENDPOINT)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, String.format(YAMMER_AUTHORIZATION_HEADER_VALUE, accessToken))
                .get(JsonNode.class);

        String yammerId = response.get("id").asText().trim();
        String name = response.get("full_name").asText();
        String imageUrlTemplate = response.get("mugshot_url").asText();

        Employee employee = new Employee(name, yammerId);
        employee.setImageUrlTemplate(imageUrlTemplate);
        return employee;
    }
}
