package com.yammer.stresstime.auth;

import java.util.Set;

public class StresstimeCredentials {

    public static StresstimeCredentials absent(Set<Role> acceptedRoles) {
        return new StresstimeCredentials(acceptedRoles);
    }

    private Set<Role> acceptedRoles; // Roles here, seriously?
    private String accessToken;
    private String yammerId;

    private StresstimeCredentials(Set<Role> acceptedRoles) {
        this.acceptedRoles = acceptedRoles;
    }

    public StresstimeCredentials(String accessToken, String yammerId, Set<Role> acceptedRoles) {
        this.accessToken = accessToken;
        this.yammerId = yammerId;
        this.acceptedRoles = acceptedRoles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getYammerId() {
        return yammerId;
    }

    public Set<Role> getAcceptedRoles() {
        return acceptedRoles;
    }

    public boolean isPresent() {
        return accessToken != null && yammerId != null;
    }
}
