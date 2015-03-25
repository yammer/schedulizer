package com.yammer.schedulizer.managers.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class SchedulizerException extends WebApplicationException {

    public SchedulizerException() {
        /* Empty */
    }

    public SchedulizerException(Response response) {
        super(response);
    }

    public SchedulizerException(int status) {
        super(status);
    }

    public SchedulizerException(Response.Status status) {
        super(status);
    }

    public SchedulizerException(Throwable cause) {
        super(cause);
    }

    public SchedulizerException(Throwable cause, Response response) {
        super(cause, response);
    }

    public SchedulizerException(Throwable cause, int status) {
        super(cause, status);
    }

    public SchedulizerException(Throwable cause, Response.Status status) {
        super(cause, status);
    }
}
