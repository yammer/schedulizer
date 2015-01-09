package com.yammer.stresstime.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("hello")
public class HelloWorldResource {
    @GET
    public String getHelloWorld() {
        return "Hello World";
    }
}
