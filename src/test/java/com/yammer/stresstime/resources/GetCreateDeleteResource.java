package com.yammer.stresstime.resources;

import com.google.common.reflect.TypeToken;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.BaseEntity;
import com.yammer.stresstime.entities.Group;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class GetCreateDeleteResource<E extends BaseEntity> extends BaseResourceTest {

    MultivaluedMap postForm;
    String resourcePath;

    @Before
    public void setup() throws Exception {
        super.setUp();
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

    @Test
    public void testCreateGetGroups() throws Exception{
        E entity = (E) resource(resourcePath).entity(postForm).post(getEntityClass());
        assertNotNull(entity);
        assertTrue(checkCreatedEntity(entity));
        List<E> entities = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        assertNotNull(entities);
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).getId(), equalTo(entity.getId()));
        assertTrue(checkCreatedEntity(entities.get(0)));
    }

    @Test
    public void testDeleteGroup() {
        E entity = (E) resource(resourcePath).entity(postForm).post(getEntityClass());
        assertNotNull(entity);
        assertTrue(checkCreatedEntity(entity));
        List<E> entities = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        assertNotNull(entities);
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).getId(), equalTo(entity.getId()));
        resource(resourcePath + "/" + entity.getId()).delete();
        List<E> empty = Arrays.asList(resource(resourcePath).get(getEntityArrayClass()));
        assertNotNull(empty);
        assertThat(empty.size(), equalTo(0));
    }

}
