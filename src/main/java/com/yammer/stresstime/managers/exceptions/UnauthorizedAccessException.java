package com.yammer.stresstime.managers.exceptions;

import com.yammer.stresstime.auth.Authentication;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class UnauthorizedAccessException extends StresstimeException {

    public UnauthorizedAccessException(String message) {
        super(Response
                .status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, Authentication.SCHEME)
                .entity(message)
                .build());
    }

    public UnauthorizedAccessException() {
        this("You are not authorized to perform this action.");
    }
}
