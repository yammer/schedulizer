package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.Group;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.ws.rs.core.MultivaluedMap;

public class GroupsResourceTest extends GetCreateDeleteResourceTest<Group> {

    private String groupName = "Core Services";

    @Override
    protected MultivaluedMap getSamplePostForm() {
        MultivaluedMapImpl values = new MultivaluedMapImpl();
        values.add("name",  groupName);
        return values;
    }

    @Override
    protected String getResourcePath() {
        return "/groups";
    }

    @Override
    protected boolean checkCreatedEntity(Group entity) {
        return entity.getName().equals(groupName);
    }

    @Override
    protected Class getEntityClass() {
        return Group.class;
    }

    @Override
    protected Class getEntityArrayClass() {
        return Group[].class;
    }
}
