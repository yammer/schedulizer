package com.yammer.schedulizer.managers.exceptions;

import org.hibernate.HibernateException;

import javax.ws.rs.core.Response;

public class HibernateUncaughtException extends SchedulizerException {

    public HibernateUncaughtException(HibernateException e) {
        super(e, Response.serverError().entity("Database exception").build());
    }
}
