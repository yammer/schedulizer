package com.yammer.stresstime.auth;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.yammer.stresstime.managers.exceptions.StresstimeException;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Set;

import static com.google.common.base.Preconditions.*;


public class StresstimeAuthorizeInjectable<T> extends AbstractHttpContextInjectable<T> {

    private final Authenticator<? super StresstimeCredentials, T> authenticator;
    private final Set<Role> roles;

    public StresstimeAuthorizeInjectable(
            Authenticator<? super StresstimeCredentials, T> authenticator,
            Role[] roles) {
        this.authenticator = authenticator;
        this.roles = ImmutableSet.copyOf(roles);
    }

    @Override
    public T getValue(HttpContext context) {
        String headerString = context.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
        StresstimeCredentials credentials;

        if (headerString != null) {
            AuthorizationHeader header;
            boolean includeErrorMessage = false;
            try {
                header = AuthorizationHeader.decode(headerString);
                includeErrorMessage = true;
                checkArgument(header.isScheme(StresstimeAuthentication.SCHEME),
                        "Scheme not supported in authorization header.");
                checkArgument(header.hasParameter(StresstimeAuthentication.Param.ACCESS_TOKEN),
                        "Param access-token not provided in header.");
                checkArgument(header.hasParameter(StresstimeAuthentication.Param.YAMMER_ID),
                        "Param yammer-id not provided in header.");
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new StresstimeException(e, Response
                        .status(Response.Status.BAD_REQUEST)
                        .header(HttpHeaders.WWW_AUTHENTICATE, StresstimeAuthentication.SCHEME)
                        .entity((includeErrorMessage) ? e.getMessage() : "Could not decode authorization header")
                        .build());
            }
            String accessToken = header.getParameter(StresstimeAuthentication.Param.ACCESS_TOKEN);
            String yammerId = header.getParameter(StresstimeAuthentication.Param.YAMMER_ID);
            credentials = new StresstimeCredentials(accessToken, yammerId, roles);
        } else {
            credentials = StresstimeCredentials.absent(roles);
        }

        Optional<T> principal;
        try {
            principal = authenticator.authenticate(credentials);
        } catch (AuthenticationException e) {
            throw new StresstimeException(Response.serverError().build());
        }

        if (!principal.isPresent()) {
            throw new StresstimeException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, SCHEME)
                    .entity("You are not authorized to perform this action.")
                    .build());
        }

        return principal.get();
    }
}
