package com.yammer.schedulizer.managers;

import com.google.common.collect.ImmutableList;
import com.yammer.schedulizer.entities.BaseEntity;
import com.yammer.schedulizer.managers.exceptions.EntityNonUniqueException;
import com.yammer.schedulizer.managers.exceptions.EntityNotFoundException;
import com.yammer.schedulizer.managers.exceptions.HibernateUncaughtException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EntityManager<E extends BaseEntity> extends AbstractDAO<E> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManager.class);
    private final Class<? extends E> entityClass;
    private static final int MAX_BATCH_SIZE = 20; // should be the same as hibernate.jdbc.batch_size property in app.yml

    public EntityManager(SessionFactory sessionFactory, Class<? extends E> entityClass) {
        super(sessionFactory);
        this.entityClass = entityClass;
    }

    public List<E> all() {
        return list(currentSession().createCriteria(entityClass));
    }

    public List<E> top(int n) {
        return list(currentSession()
                .createCriteria(entityClass)
                .setMaxResults(n));
    }

    public long count() {
        return ((Number) currentSession()
                .createCriteria(entityClass)
                .setProjection(Projections.rowCount())
                .uniqueResult())
                .longValue();
    }

    public void save(E entity) {
        try {
            persist(entity);
        } catch (HibernateException e) {
            throw new HibernateUncaughtException(e);
        }
    }

    public void save(Iterable<? extends E> entities) {
        try {
            int i = 0;
            for (E entity : entities) {
                save(entity);
                if (i % MAX_BATCH_SIZE == MAX_BATCH_SIZE - 1) {
                    currentSession().flush();
                    currentSession().clear();
                }
                i++;
            }
        } catch (HibernateException e) {
            throw new HibernateUncaughtException(e);
        }
    }

    public boolean safeSave(E entity) {
        try {
            save(entity);
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to save {}.", entity, e);
            return false;
        }
    }

    public void refresh(Iterable<? extends E> entities) {
        currentSession().flush();
        for (E entity : entities) {
            currentSession().refresh(entity);
        }
    }

    public void refresh(E... entities) {
        refresh(ImmutableList.copyOf(entities));
    }

    // Needs to be a valid entity
    public void delete(E entity) {
        currentSession().delete(entity);
    }

    public boolean safeDelete(E entity) {
        try {
            delete(entity);
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to delete {}.", entity, e);
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
        } catch (Exception e) {
            LOG.warn("Unable to delete entity with id {}.", id, e);
            return false;
        }
    }

    protected <T> T getExactOne(Criteria criteria, Class<T> klass) {
        T entity = getUnique(criteria, klass);
        return checkFound(entity, klass);
    }

    protected E getExactOne(Criteria criteria) {
        return getExactOne(criteria, entityClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getUnique(Criteria criteria, Class<? extends T> klass) {
        try {
            return (T) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new EntityNonUniqueException(e, klass);
        }
    }

    protected E getUnique(Criteria criteria) {
        return getUnique(criteria, entityClass);
    }

    protected <T> T checkFound(T entity, Class<? extends T> klass) {
        if (entity == null) {
            throw new EntityNotFoundException(klass);
        }
        return entity;
    }

    protected E checkFound(E entity) {
        return checkFound(entity, entityClass);
    }
}
