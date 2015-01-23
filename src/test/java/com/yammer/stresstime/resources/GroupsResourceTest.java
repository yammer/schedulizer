package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class GroupsResourceTest {

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new GroupsResource(new GroupManager(new DatabaseMock(Arrays.<Object>asList(
                    new Group("group1"),
                    new Group("group2"),
                    new Group("group3"),
                    new Group("group4"))).getMock())))
            .build();

    @Test
    public void testGetPerson() {
        RESOURCES.client();

        assertThat(RESOURCES.client().resource("/groups/test1?s=test").get(String.class)).isEqualTo("test");

        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("s", "test");

        assertThat(RESOURCES.client().resource("/groups/test1").post(String.class, formData)).isEqualTo("test");
    }
}