package com.yammer.stresstime.managers.exceptions;

import javax.ws.rs.core.Response;

public class EntityNotFoundException extends StresstimeException {

    public EntityNotFoundException(Class<?> klass) {
        super(Response
                .status(Response.Status.BAD_REQUEST)
                .entity(String.format("Entity %s not found", klass.getSimpleName()))
                .build());
    }

    public EntityNotFoundException(Class<?> klass, long id) {
        super(Response
                .status(Response.Status.BAD_REQUEST)
                .entity(String.format("%s with id %d not found", klass.getSimpleName(), id))
                .build());
    }
}
