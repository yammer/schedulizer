package com.yammer.schedulizer.managers.exceptions;

import com.yammer.schedulizer.auth.Authentication;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class UnauthorizedAccessException extends SchedulizerException {

    public UnauthorizedAccessException(String message) {
        super(Response
                .status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, Authentication.SCHEME)
                .entity(message)
                .build());
    }

    public UnauthorizedAccessException() {
        this("{ \"message\": \"You are not authorized to perform this action.\" }");
    }
}
