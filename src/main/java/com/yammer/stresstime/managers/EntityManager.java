package com.yammer.stresstime.managers;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

public class EntityManager<E> extends AbstractDAO<E> {

    public EntityManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public void save(E entity) {
        persist(entity);
    }

    public void delete(E entity) {
        currentSession().delete(entity);
    }
}
