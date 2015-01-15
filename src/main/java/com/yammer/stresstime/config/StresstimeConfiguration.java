package com.yammer.stresstime.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class StresstimeConfiguration extends Configuration {

    private final String mRootPath;

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    public StresstimeConfiguration(@JsonProperty("rootPath") String rootPath) {
        mRootPath = rootPath;
    }

    public String getRootPath() {
        return mRootPath;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }
}
