package com.yammer.schedulizer.test;

import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.SchedulizerApplication;
import com.yammer.schedulizer.auth.AbstractAuthenticator;
import com.yammer.schedulizer.auth.AuthorizeProvider;
import com.yammer.schedulizer.auth.MockAuthenticator;
import com.yammer.schedulizer.config.SchedulizerConfiguration;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.UserManager;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class TestApplication extends SchedulizerApplication {
    @Override
    protected void registerAuthenticator(SchedulizerConfiguration config, Environment env) {
        Client client = new JerseyClientBuilder(env)
                .using(config.getJerseyClientConfiguration())
                .build(getName());
        AbstractAuthenticator authenticator = new MockAuthenticator(client,
                new UserManager(getSessionFactory()),
                new EmployeeManager(getSessionFactory()));
        env.jersey().register(new AuthorizeProvider<>(authenticator));
    }
}
