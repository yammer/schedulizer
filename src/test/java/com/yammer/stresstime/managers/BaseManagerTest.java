package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.BaseEntity;
import com.yammer.stresstime.test.DatabaseTest;

import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public abstract class BaseManagerTest<Entity extends BaseEntity> extends DatabaseTest {

    EntityManager<Entity> manager;
    List<Entity> entities;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        initialize();
        this.manager = getEntityManager();
        this.entities = getEntities();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        clean();
        super.tearDown();
    }

    protected abstract EntityManager<Entity> getEntityManager();
    protected abstract List<Entity> getEntities();
    protected abstract void initialize();
    protected abstract void clean();

    @Test
    public void testSaveGetAndDelete() {
        Entity entity = entities.get(0);
        // Save getById delete
        manager.save(entity);
        Entity e = manager.getById(entity.getId());
        assertNotNull(e);
        assertThat(e, equalTo(entity));
        manager.delete(entity);
        Entity nullEntity = manager.safeGetById(entity.getId());
        assertNull(nullEntity);

        // DeleteById
        manager.save(entity);
        e = manager.getById(entity.getId());
        assertNotNull(e);
        assertThat(e, equalTo(entity));
        manager.deleteById(entity.getId());
        nullEntity = manager.safeGetById(entity.getId());
        assertNull(nullEntity);

    }

    @Test
    public void testCount() {
        long count = 0;
        assertThat(manager.count(), equalTo(count));
        for (Entity e : entities) {
            manager.save(e);
            assertThat(manager.count(), equalTo(++count));
        }
        for (Entity e : entities) {
            manager.delete(e);
            assertThat(manager.count(), equalTo(--count));
        }
    }

    @Test
    public void testAll() {
        for (Entity e : entities) {
            manager.save(e);
        }
        assertArrayEquals(manager.all().toArray(), entities.toArray());
        for (Entity e : entities) {
            manager.delete(e);
        }
    }
}
