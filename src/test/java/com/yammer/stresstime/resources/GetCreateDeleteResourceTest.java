package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.BaseEntity;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class GetCreateDeleteResourceTest<E extends BaseEntity> extends BaseResourceTest {

    private MultivaluedMap postForm;
    private String resourcePath;
    private int initialSize; // In case the initialize function creates entities that will be retrieved by the resource

    @Before
    public void setup() throws Exception {
        super.setUp();
        initialSize = 0;
        initialize();
        postForm = getSamplePostForm();
        resourcePath = getResourcePath();
        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length()-1);
        }
    }

    protected abstract MultivaluedMap getSamplePostForm();
    protected abstract String getResourcePath();
    protected abstract boolean checkCreatedEntity(E entity);
    protected abstract Class<E> getEntityClass();
    protected abstract Class<E[]> getEntityArrayClass();
    protected abstract void initialize();

    protected void setNumberCreatedEntities(int n) {
        initialSize = n;
    }

    @Test
    public void testCreateGet() throws Exception{
        E entity = (E) resource(resourcePath).entity(postForm).post(getEntityClass());
        assertNotNull(entity);
        assertTrue(checkCreatedEntity(entity));
        List<E> entities = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        entities.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
        assertNotNull(entities);
        assertThat(entities.size(), equalTo(initialSize + 1));
        assertThat(entities.get(initialSize).getId(), equalTo(entity.getId()));
        assertTrue(checkCreatedEntity(entities.get(initialSize)));
    }

    @Test
    public void testDelete() {
        E entity = (E) resource(resourcePath).entity(postForm).post(getEntityClass());
        assertNotNull(entity);
        assertTrue(checkCreatedEntity(entity));
        List<E> entities = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        assertNotNull(entities);
        assertThat(entities.size(), equalTo(initialSize + 1));
        entities.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
        assertThat(entities.get(initialSize).getId(), equalTo(entity.getId()));
        resource(resourcePath + "/" + entity.getId()).delete();
        List<E> empty = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        assertNotNull(empty);
        assertThat(empty.size(), equalTo(initialSize));
    }

}
