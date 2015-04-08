package com.yammer.schedulizer.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.Client;
import com.yammer.schedulizer.entities.Employee;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class FacebookAuthenticator extends ExtAppAuthenticator {
    private static final String FACEBOOK_CURRENT_USER_ENDPOINT = "https://graph.facebook.com/v2.3/me";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String FIELDS_PARAM = "fields";
    private static final String REQUIRED_FIELDS = "name, id, picture";


    public FacebookAuthenticator(Client client) {
        super(client);
    }

    @Override
    public Employee getTokenOwner(String accessToken) {
        JsonNode response = client.resource(FACEBOOK_CURRENT_USER_ENDPOINT)
                .queryParam(ACCESS_TOKEN_PARAM, accessToken)
                .queryParam(FIELDS_PARAM, REQUIRED_FIELDS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(JsonNode.class);

        String yammerId = response.get("id").asText().trim();
        String name = response.get("name").asText();
        String imageUrlTemplate = response.get("picture").get("data").get("url").asText();

        Employee employee = new Employee(name, yammerId, ExtAppType.facebook);
        employee.setImageUrlTemplate(imageUrlTemplate);
        return employee;
    }
}
