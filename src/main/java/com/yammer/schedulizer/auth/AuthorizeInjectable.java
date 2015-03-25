package com.yammer.schedulizer.auth;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.yammer.schedulizer.managers.exceptions.SchedulizerException;
import com.yammer.schedulizer.managers.exceptions.UnauthorizedAccessException;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;


public class AuthorizeInjectable<T> extends AbstractHttpContextInjectable<T> {

    private final Authenticator<? super Credentials, T> authenticator;
    private final Set<Role> roles;

    public AuthorizeInjectable(
            Authenticator<? super Credentials, T> authenticator,
            Role[] roles) {
        this.authenticator = authenticator;
        this.roles = ImmutableSet.copyOf(roles);
    }

    @Override
    public T getValue(HttpContext context) {
        String headerString = context.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
        Credentials credentials;

        if (headerString != null) {
            AuthorizationHeader header;
            boolean includeErrorMessage = false;
            try {
                header = AuthorizationHeader.decode(headerString);
                includeErrorMessage = true;
                checkArgument(header.isScheme(Authentication.SCHEME),
                        "Scheme not supported in authorization header.");
                checkArgument(header.hasParameter(Authentication.Param.ACCESS_TOKEN),
                        "Param access-token not provided in header.");
                checkArgument(header.hasParameter(Authentication.Param.EXT_API_ID),
                        "Param yammer-id not provided in header.");
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new SchedulizerException(e, Response
                        .status(Response.Status.BAD_REQUEST)
                        .header(HttpHeaders.WWW_AUTHENTICATE, Authentication.SCHEME)
                        .entity((includeErrorMessage) ? e.getMessage() : "Could not decode authorization header")
                        .build());
            }
            String accessToken = header.getParameter(Authentication.Param.ACCESS_TOKEN);
            String yammerId = header.getParameter(Authentication.Param.EXT_API_ID);
            credentials = new Credentials(accessToken, yammerId, roles);
        } else {
            credentials = Credentials.absent(roles);
        }

        Optional<T> principal;
        try {
            principal = authenticator.authenticate(credentials);
        } catch (AuthenticationException e) {
            throw new SchedulizerException(Response.serverError().build());
        }

        if (!principal.isPresent()) {
            throw new UnauthorizedAccessException();
        }

        return principal.get();
    }
}
