package com.yammer.stresstime.managers.exceptions;

import javax.ws.rs.core.Response;

public class EntityNonUniqueException extends StresstimeException {

    public EntityNonUniqueException(Class<?> klass) {
        this(null, klass);
    }

    public EntityNonUniqueException(Throwable cause, Class<?> klass) {
        super(cause, Response
                .serverError()
                .entity(String.format(
                        "Entity %s expected to be unique within context but it isn't", klass.getSimpleName()))
                .build());
    }
}
