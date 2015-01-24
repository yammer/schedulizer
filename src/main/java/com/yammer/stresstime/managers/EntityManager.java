package com.yammer.stresstime.managers;

import com.yammer.stresstime.managers.exceptions.EntityNotFoundException;
import com.yammer.stresstime.managers.exceptions.HibernateUncaughtException;
import com.yammer.stresstime.managers.exceptions.StresstimeException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

public class EntityManager<E> extends AbstractDAO<E> {

    private final Class<? extends E> mEntityClass;

    public EntityManager(SessionFactory sessionFactory, Class<? extends E> entityClass) {
        super(sessionFactory);
        mEntityClass = entityClass;
    }

    public void save(E entity) {
        try {
            persist(entity);
        } catch (HibernateException e) {
            throw new HibernateUncaughtException(e);
        }
    }

    public boolean safeSave(E entity) {
        try {
            save(entity);
            return true;
        } catch (StresstimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Needs to be a valid entity
    public void delete(E entity) {
        currentSession().delete(entity);
    }

    public boolean safeDelete(E entity) {
        try {
            delete(entity);
            return true;
        } catch (StresstimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public E getById(long id) {
        E entity = safeGetById(id);
        if (entity == null) {
            throw new EntityNotFoundException(mEntityClass, id);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public E safeGetById(long id) {
        return (E) currentSession().get(mEntityClass, id);
    }

    public void deleteById(long id) {
        delete(getById(id));
    }

    public boolean safeDeleteById(long id) {
        try {
            deleteById(id);
            return true;
        } catch (StresstimeException e) {
            e.printStackTrace();
            return false;
        }
    }
}
