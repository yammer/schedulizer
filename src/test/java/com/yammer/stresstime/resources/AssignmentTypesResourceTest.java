package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.fixtures.GroupsFixture;

import javax.ws.rs.core.MultivaluedMap;

public class AssignmentTypesResourceTest extends GetCreateDeleteResourceTest<AssignmentType> {
    private Group group;
    String name = "Primary";
    String description = "blah blah blah";

    @Override
    protected MultivaluedMap getSamplePostForm() {
        MultivaluedMapImpl form = new MultivaluedMapImpl();
        form.add("name", name);
        form.add("description", description);
        return form;
    }

    @Override
    protected String getResourcePath() {
        return String.format("/groups/%d/assignment-types", group.getId());
    }

    @Override
    protected boolean checkCreatedEntity(AssignmentType entity) {
        return entity.getName().equals(name) && entity.getDescription().equals(description);
    }

    @Override
    protected Class<AssignmentType> getEntityClass() {
        return AssignmentType.class;
    }

    @Override
    protected Class<AssignmentType[]> getEntityArrayClass() {
        return AssignmentType[].class;
    }

    @Override
    protected void initialize() {
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        group = groupsFixture.getGroups().get(0);
        refresh(group);
    }
}
