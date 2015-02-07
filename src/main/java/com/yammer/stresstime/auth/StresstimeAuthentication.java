package com.yammer.stresstime.auth;

// Authorization header syntax
// Authorization: ST-AUTH access-token = <value>, yammer-id = <value>
public class StresstimeAuthentication {

    public static final String SCHEME = "ST-AUTH";

    public static class Param {
        public static final String ACCESS_TOKEN = "access-token";
        public static final String YAMMER_ID = "yammer-id";
    }
    
    // Prevents instantiation
    private StresstimeAuthentication() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
