package com.yammer.stresstime.managers;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

public class EntityManager<E> extends AbstractDAO<E> {

    private final Class<? extends E> mEntityClass;

    public EntityManager(SessionFactory sessionFactory, Class<? extends E> entityClass) {
        super(sessionFactory);
        mEntityClass = entityClass;
    }

    public void save(E entity) {
         persist(entity);
    }

    // needs to be a valid entity
    public void delete(E entity) {
        currentSession().delete(entity);
    }

    public boolean safeDelete(E entity) {
        try {
            delete(entity);
            return true;
        } catch (Exception e) {
            /* TODO: Logging to proper stream  */
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public E getById(long id) {
        return (E) currentSession().get(mEntityClass, id);
    }

    // issues two queries but deletes only if id exists
    public boolean deleteById(long id) {
        Object persistentInstance = getById(id);
        if (persistentInstance != null) {
            currentSession().delete(persistentInstance);
            return true;
        }
        return false;
    }
}
