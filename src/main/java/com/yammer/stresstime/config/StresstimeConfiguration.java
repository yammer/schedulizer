package com.yammer.stresstime.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class StresstimeConfiguration extends Configuration {

    private final String rootPath;

    private final String authenticatorClass;

    @Valid @NotNull @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid @NotNull @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public StresstimeConfiguration(@JsonProperty("rootPath") String rootPath,
                                   @JsonProperty("authenticatorClass") String authenticatorClass) {
        this.rootPath = rootPath;
        this.authenticatorClass = authenticatorClass;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getAuthenticatorClass() { return authenticatorClass; }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
