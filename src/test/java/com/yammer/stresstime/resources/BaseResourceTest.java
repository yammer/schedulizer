package com.yammer.stresstime.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.yammer.stresstime.StresstimeApplication;
import com.yammer.stresstime.TestSuite;
import com.yammer.stresstime.auth.Authenticator;
import com.yammer.stresstime.entities.BaseEntity;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.*;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseResourceTest  extends DatabaseTest {

    private Client client;
    private Employee globalAdmin;
    private Employee currentUser;

    public BaseResourceTest() {
        client = new Client();
        globalAdmin = new Employee("Global Admin", TestUtils.nextYammerId());
        globalAdmin.setGlobalAdmin(true);
        Session session = getSessionFactory().openSession();;
        session.save(globalAdmin);
        session.flush();
        session.close();
        setCurrentUser(globalAdmin);
    }

    protected void setCurrentUser(Employee employee) {
        currentUser = employee;
    }

    protected Employee getGlobalAdmin() {
        return globalAdmin;
    }

    protected WebResource.Builder resource(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        WebResource.Builder resource = client.resource(
                String.format("http://localhost:%d/service/%s", TestSuite.RULE.getLocalPort(), path))
                    .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        if (currentUser != null) {
            return resource.header("Authorization", getAuthorizationHeader());
        }
        return resource;
    }

    private String getAuthorizationHeader() {
        return String.format("ST-AUTH access-token = \"\", yammer-id = \"%s\"", currentUser.getYammerId());
    }
}
