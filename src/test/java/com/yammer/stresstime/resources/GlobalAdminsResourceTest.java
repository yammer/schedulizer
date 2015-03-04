package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.test.TestUtils;

import javax.ws.rs.core.MultivaluedMap;

public class GlobalAdminsResourceTest extends GetCreateDeleteResourceTest<Employee> {

    private String employeeName = "Luiz Filipe";
    private String imageUrlTemplate = "imageUrlTemplate";
    private String yammerId = TestUtils.nextYammerId();
    private Group group;

    @Override
    protected MultivaluedMap getSamplePostForm() {
        MultivaluedMapImpl values = new MultivaluedMapImpl();
        values.add("name",  employeeName);
        values.add("yammerId", yammerId);
        values.add("imageUrlTemplate", imageUrlTemplate);
        return values;
    }

    @Override
    protected String getResourcePath() {
        return "/employees/admins";
    }

    @Override
    protected boolean checkCreatedEntity(Employee entity) {
        return entity.getName().equals(employeeName) &&
                entity.getImageUrlTemplate().equals(imageUrlTemplate) &&
                entity.getYammerId().equals(yammerId);
    }

    @Override
    protected Class getEntityClass() {
        return Employee.class;
    }

    @Override
    protected Class getEntityArrayClass() {
        return Employee[].class;
    }

    @Override
    protected void initialize() {
        setNumberCreatedEntities(1); // Database test create a global admin by default
    }
}
