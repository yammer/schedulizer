package com.yammer.stresstime.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.yammer.stresstime.auth.AbstractAuthenticator;

public class AuthenticatorHealthCheck extends HealthCheck {

    private AbstractAuthenticator authenticator;

    public AuthenticatorHealthCheck(AbstractAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected Result check() throws Exception {
        if (authenticator == null) {
            return Result.unhealthy("Unable to initialize authenticator");
        }
        return Result.healthy();
    }
}
