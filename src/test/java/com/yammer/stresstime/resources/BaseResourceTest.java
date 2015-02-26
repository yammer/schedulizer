package com.yammer.stresstime.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.TestSuite;
import com.yammer.stresstime.auth.Authenticator;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.UserManager;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.hibernate.Session;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class BaseResourceTest  extends DatabaseTest {

    private Client client;
    protected Employee globalAdmin;

    public BaseResourceTest() {
        client = new Client();
        globalAdmin = new Employee("Global Admin", TestUtils.nextYammerId());
        globalAdmin.setGlobalAdmin(true);
        Session session = getSessionFactory().openSession();;
        session.save(globalAdmin);
        session.flush();
        session.close();
    }

    public WebResource.Builder resource(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        return client.resource(
                String.format("http://localhost:%d/service/%s", TestSuite.RULE.getLocalPort(), path))
                    .header("Authorization", String.format("ST-AUTH access-token = \"\", yammer-id = \"%s\"", globalAdmin.getYammerId()))
                    .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }
}
