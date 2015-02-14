package com.yammer.stresstime.auth;

import com.google.common.base.Objects;

import java.util.Set;

public class Credentials {

    public static Credentials absent(Set<Role> acceptedRoles) {
        return new Credentials(acceptedRoles);
    }

    private Set<Role> acceptedRoles; // Roles here, seriously?
    private String accessToken;
    private String yammerId;

    private Credentials(Set<Role> acceptedRoles) {
        this.acceptedRoles = acceptedRoles;
    }

    public Credentials(String accessToken, String yammerId, Set<Role> acceptedRoles) {
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

    @Override
    public String toString() {
        return Objects.toStringHelper(Credentials.class)
                .add("accessToken", accessToken)
                .add("yammerId", yammerId)
                .add("acceptedRoles", acceptedRoles)
                .toString();
    }
}
