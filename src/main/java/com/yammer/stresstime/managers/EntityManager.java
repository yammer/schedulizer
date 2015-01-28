package com.yammer.stresstime.managers;

import com.yammer.stresstime.managers.exceptions.EntityNonUniqueException;
import com.yammer.stresstime.managers.exceptions.EntityNotFoundException;
import com.yammer.stresstime.managers.exceptions.HibernateUncaughtException;
import com.yammer.stresstime.managers.exceptions.StresstimeException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.List;

public class EntityManager<E> extends AbstractDAO<E> {

    private final Class<? extends E> entityClass;
    private static final int MAX_BATCH_SIZE = 20; // should be the same as hibernate.jdbc.batch_size property in app.yml

    public EntityManager(SessionFactory sessionFactory, Class<? extends E> entityClass) {
        super(sessionFactory);
        this.entityClass = entityClass;
    }

    public void save(E entity) {
        try {
            persist(entity);
        } catch (HibernateException e) {
            throw new HibernateUncaughtException(e);
        }
    }

    public void save(List<E> entities) {
        try {
            for (int i = 0; i < entities.size(); i++) {
                persist(entities.get(i));
                if (i % MAX_BATCH_SIZE == MAX_BATCH_SIZE - 1) {
                    currentSession().flush();
                    currentSession().clear();
                }
            }

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
            throw new EntityNotFoundException(entityClass, id);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public E safeGetById(long id) {
        return (E) currentSession().get(entityClass, id);
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

    protected E getExactOne(Criteria criteria) {
        E entity = getUnique(criteria);
        if (entity == null) {
            throw new EntityNotFoundException(entityClass);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    protected E getUnique(Criteria criteria) {
        try {
            return (E) criteria.uniqueResult();
        } catch (HibernateException e) {
            EntityNonUniqueException error = new EntityNonUniqueException(entityClass);
            error.initCause(e);
            throw error;
        }
    }
}
