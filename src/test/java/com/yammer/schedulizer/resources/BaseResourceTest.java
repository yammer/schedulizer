package com.yammer.schedulizer.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.yammer.schedulizer.TestSuite;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.test.DatabaseTest;
import com.yammer.schedulizer.test.TestUtils;
import org.hibernate.Session;

import javax.ws.rs.core.MediaType;

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
        return String.format("SC-AUTH access-token = \"\", ext-app-id = \"%s\"", currentUser.getYammerId());
    }
}
