package com.yammer.stresstime.managers;

import com.yammer.stresstime.managers.exceptions.EntityNotFoundException;
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
    public E safeGetById(long id) {
        return (E) currentSession().get(mEntityClass, id);
    }

    public E getById(long id) {
        E entity = safeGetById(id);
        if (entity == null) {
            throw new EntityNotFoundException(mEntityClass, id);
        }
        return entity;
    }

    // issues two queries but deletes only if id exists
    public boolean deleteById(long id) {
        Object persistentInstance = safeGetById(id);
        if (persistentInstance != null) {
            currentSession().delete(persistentInstance);
            return true;
        }
        return false;
    }
}
