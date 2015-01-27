package com.yammer.stresstime.managers.exceptions;

import org.hibernate.HibernateException;

import javax.ws.rs.core.Response;

public class HibernateUncaughtException extends StresstimeException {

    public HibernateUncaughtException(HibernateException e) {
        super(Response.serverError().entity("Database exception").build());
        initCause(e);
    }
}
