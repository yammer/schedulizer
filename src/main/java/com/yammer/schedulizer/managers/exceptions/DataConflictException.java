package com.yammer.schedulizer.managers.exceptions;

import javax.ws.rs.core.Response;

public class DataConflictException extends SchedulizerException {

    public DataConflictException() {
        super(Response
                .status(Response.Status.CONFLICT)
                .entity("Data provided in the request does not match retrieved data")
                .build());
    }

    public DataConflictException(Class<?> klass) {
        super(Response
                .status(Response.Status.CONFLICT)
                .entity(String.format(
                        "Data provided in the request for %s entity does not match retrieved data",
                        klass.getSimpleName()))
                .build());
    }
}
