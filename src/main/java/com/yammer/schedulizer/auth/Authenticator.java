package com.yammer.schedulizer.auth;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.UserManager;
import com.yammer.schedulizer.utils.CoreUtils;
import io.dropwizard.auth.AuthenticationException;

import javax.ws.rs.core.Response;
import java.util.Set;

public class Authenticator extends AbstractAuthenticator {

    private static final int EXT_APP_REQUEST_RETRIES = 3;

    public Authenticator(Client client, UserManager userManager,
                         EmployeeManager employeeManager, ExtAppAuthenticator extAppAuthenticator,
                         ExtAppAuthenticatorFactory.ExtAppType extAppType) {
        super(client, userManager, employeeManager, extAppAuthenticator, extAppType);
    }

    @Override
    public Optional<User> authenticate(Credentials credentials) throws AuthenticationException {
        User user;
        if (!credentials.isPresent()) {
            user = User.guest();
        } else {
            String extAppId = credentials.getExtAppId();
            Employee employee = employeeManager.safeGetByExtAppId(extAppId);
            if (employee == null) {
                employee = getTokenOwner(credentials);

                if (employee == null || !employee.getExtAppId().equals(extAppId)) {
                    return Optional.absent();
                }
                if (userManager.count(extAppType) == 0) {
                    // First employee to login with the external app is a global admin
                    employee.setGlobalAdmin(true);
                }
                // User verified successfully
                employeeManager.save(employee);
                user = User.fresh(employee, credentials.getAccessToken(), extAppType);
            } else {
                user = employee.getUser();
                if (user == null) {
                    user = new User(employee, credentials.getAccessToken(), extAppType);
                } else if (!credentials.getAccessToken().equals(user.getAccessToken())) {
                    user.setAccessToken(credentials.getAccessToken());
                    user.expire();
                }
            }
        }
        return authenticate(user, credentials);
    }

    // Don't save the user if cannot verify
    private Optional<User> authenticate(User user, Credentials credentials) throws AuthenticationException {
        Set<Role> acceptedRoles = credentials.getAcceptedRoles();
        if (!user.isGuest()) {
            if (!user.isUpToDate()) {
                // Don't need to save the employee, just verify identity
                Employee tokenOwner = getTokenOwner(credentials);
                if (tokenOwner != null && tokenOwner.getExtAppId().equals(user.getEmployee().getExtAppId())) {
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
    protected Employee getTokenOwner(Credentials credentials) throws AuthenticationException {
        String accessToken = credentials.getAccessToken();
        Exception last = null;
        for (int i = 0; i < EXT_APP_REQUEST_RETRIES; i++) {
            try {
                return extAppAuthenticator.getTokenOwner(accessToken);
            } catch (Exception exception) {
                Optional<UniformInterfaceException> cause =
                        CoreUtils.getCause(exception, UniformInterfaceException.class);
                if (cause.isPresent() &&
                        (cause.get().getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode() ||
                            cause.get().getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode())) {
                    // yammer returns unauthorized but facebook returns bad request
                    return null;
                }
                last = exception;
                exception.printStackTrace();
            }
        }
        throw new AuthenticationException(last);
    }
}
