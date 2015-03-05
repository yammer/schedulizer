package com.yammer.stresstime.test;

import com.sun.jersey.api.client.Client;
import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.auth.AbstractAuthenticator;
import com.yammer.stresstime.auth.AuthorizeProvider;
import com.yammer.stresstime.auth.MockAuthenticator;
import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.UserManager;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class TestApplication extends StresstimeApplication {
    @Override
    protected void registerAuthenticator(StresstimeConfiguration config, Environment env) {
        Client client = new JerseyClientBuilder(env)
                .using(config.getJerseyClientConfiguration())
                .build(getName());
        AbstractAuthenticator authenticator = new MockAuthenticator(client,
                new UserManager(getSessionFactory()),
                new EmployeeManager(getSessionFactory()));
        env.jersey().register(new AuthorizeProvider<>(authenticator));
    }
}
