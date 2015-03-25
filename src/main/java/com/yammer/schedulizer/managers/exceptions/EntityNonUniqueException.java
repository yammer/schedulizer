package com.yammer.schedulizer.managers.exceptions;

import javax.ws.rs.core.Response;

public class EntityNonUniqueException extends SchedulizerException {

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
