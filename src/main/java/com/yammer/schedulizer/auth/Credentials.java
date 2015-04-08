package com.yammer.schedulizer.auth;

import com.google.common.base.Objects;

import java.util.Set;

public class Credentials {

    public static Credentials absent(Set<Role> acceptedRoles) {
        return new Credentials(acceptedRoles);
    }

    private Set<Role> acceptedRoles; // Roles here, seriously?
    private String accessToken;
    private String extAppId;

    private Credentials(Set<Role> acceptedRoles) {
        this.acceptedRoles = acceptedRoles;
    }

    public Credentials(String accessToken, String extAppId, Set<Role> acceptedRoles) {
        this.accessToken = accessToken;
        this.extAppId = extAppId;
        this.acceptedRoles = acceptedRoles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExtAppId() {
        return extAppId;
    }

    public Set<Role> getAcceptedRoles() {
        return acceptedRoles;
    }

    public boolean isPresent() {
        return accessToken != null && extAppId != null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(Credentials.class)
                .add("accessToken", accessToken)
                .add("extAppId", extAppId)
                .add("acceptedRoles", acceptedRoles)
                .toString();
    }
}
