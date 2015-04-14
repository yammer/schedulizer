package com.yammer.schedulizer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SchedulizerConfiguration extends Configuration {

    private final String rootPath;
    private final String extApp;

    private final String extAppClientId;

    @Valid @NotNull @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid @NotNull @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public SchedulizerConfiguration(@JsonProperty("rootPath") String rootPath,
                                    @JsonProperty("extApp") String extApp,
                                    @JsonProperty("extAppClientId") String extAppClientId) {
        this.rootPath = rootPath;
        this.extApp = extApp;
        this.extAppClientId = extAppClientId;
    }

    public String getRootPath() {
        return rootPath;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return httpClient;
    }

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public String getExtApp() { return extApp; }

    public String getExtAppClientId() { return extAppClientId; }
}
