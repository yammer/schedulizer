package com.yammer.stresstime.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.TestSuite;
import com.yammer.stresstime.auth.Authenticator;
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
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class GroupsResourceTest extends BaseResourceTest {

    @Test
    public void test() throws Exception{
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("name", "12345"));
        MultivaluedMapImpl values = new MultivaluedMapImpl();
        values.add("name", "report.zip");
        resource("/groups").entity(values).post();

        List<Group> response = resource("/groups").get(List.class);
        System.out.println(response);
    }
}
