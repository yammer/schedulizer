package com.yammer.schedulizer.managers.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ParameterException extends WebApplicationException {

    public ParameterException(String parameter) {
        super(Response
                .status(Response.Status.BAD_REQUEST)
                .entity(String.format("Parameter %s is invalid or is missing", parameter))
                .build());
    }
}
