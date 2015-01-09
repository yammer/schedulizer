package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.resources.HelloWorldResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class StresstimeApplication extends Application<StresstimeConfiguration> {

    public static void main(String[] args) throws Exception {
        new StresstimeApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<StresstimeConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    public String getName() {
        return "stresstime";
    }

    @Override
    public void run(StresstimeConfiguration config, Environment env) throws Exception {
        env.jersey().register(new HelloWorldResource());
        env.jersey().setUrlPattern(config.getRootPath());
    }
}
