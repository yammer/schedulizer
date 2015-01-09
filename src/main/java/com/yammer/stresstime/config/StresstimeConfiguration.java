package com.yammer.stresstime.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class StresstimeConfiguration extends Configuration {

    private final String rootPath;

    public StresstimeConfiguration(@JsonProperty("rootPath") String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }
}
