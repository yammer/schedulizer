package com.yammer.stresstime.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test")
public class TestResource {

    @GET
    @UnitOfWork
    public String test() {
        return "hello world";
    }
}
