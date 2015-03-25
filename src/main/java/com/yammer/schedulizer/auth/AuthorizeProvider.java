package com.yammer.schedulizer.auth;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.Authenticator;

public class AuthorizeProvider<T> implements InjectableProvider<Authorize, Parameter> {

    private Authenticator<? super Credentials, T> authenticator;

    public AuthorizeProvider(Authenticator<? super Credentials, T> authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext ic, Authorize authorize, Parameter parameter) {
        return new AuthorizeInjectable<>(authenticator, authorize.value());
    }
}
