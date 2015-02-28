package com.yammer.stresstime.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.TestSuite;
import com.yammer.stresstime.auth.Authenticator;
import com.yammer.stresstime.entities.BaseEntity;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.UserManager;
import com.yammer.stresstime.test.DatabaseTest;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupsResourceTest extends GetCreateDeleteResource<Group> {

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
