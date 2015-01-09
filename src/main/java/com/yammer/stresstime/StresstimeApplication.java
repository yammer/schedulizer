package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.resources.HelloWorldResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class StresstimeApplication extends Application<StresstimeConfiguration> {
    public static void main(String[] args) throws Exception {
        new StresstimeApplication().run(args);
    }
    @Override
    public void run(StresstimeConfiguration stresstimeConfiguration, Environment environment) throws Exception {
        environment.jersey().register(new HelloWorldResource());
    }
}
