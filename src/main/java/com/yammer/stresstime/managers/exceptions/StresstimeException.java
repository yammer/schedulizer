package com.yammer.stresstime.managers.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class StresstimeException extends WebApplicationException {

    public StresstimeException() {
        /* Empty */
    }

    public StresstimeException(Response response) {
        super(response);
    }

    public StresstimeException(int status) {
        super(status);
    }

    public StresstimeException(Response.Status status) {
        super(status);
    }

    public StresstimeException(Throwable cause) {
        super(cause);
    }

    public StresstimeException(Throwable cause, Response response) {
        super(cause, response);
    }

    public StresstimeException(Throwable cause, int status) {
        super(cause, status);
    }

    public StresstimeException(Throwable cause, Response.Status status) {
        super(cause, status);
    }
}
