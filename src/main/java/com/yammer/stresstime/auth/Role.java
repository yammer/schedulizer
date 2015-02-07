package com.yammer.stresstime.auth;

public enum Role {
    ADMIN, MEMBER, GUEST;

    public static final Role[] ALL = Role.values();
}
