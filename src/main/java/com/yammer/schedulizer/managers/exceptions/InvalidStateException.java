package com.yammer.schedulizer.managers.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidStateException extends WebApplicationException {
    public InvalidStateException(String message) {
        super(Response
                .status(Response.Status.BAD_REQUEST)
                .entity(message)
                .build());
    }

    public InvalidStateException() {
        this("This request would put the database in an invalid state");
    }
}
