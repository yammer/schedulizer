package com.yammer.schedulizer.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.entities.Employee;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class YammerAuthenticator extends ExtAppAuthenticator {
    private static final String YAMMER_CURRENT_USER_ENDPOINT = "https://www.yammer.com/api/v1/users/current.json";
    private static final String YAMMER_AUTHORIZATION_HEADER_VALUE = "Bearer %s";

    public YammerAuthenticator(Client client) {
        super(client);
    }

    @Override
    public Employee getTokenOwner(String accessToken) {
        JsonNode response = client.resource(YAMMER_CURRENT_USER_ENDPOINT)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, String.format(YAMMER_AUTHORIZATION_HEADER_VALUE, accessToken))
                .get(JsonNode.class);

        String yammerId = response.get("id").asText().trim();
        String name = response.get("full_name").asText();
        String imageUrlTemplate = response.get("mugshot_url").asText();

        Employee employee = new Employee(name, yammerId);
        employee.setImageUrlTemplate(imageUrlTemplate);
        return employee;
    }
}
