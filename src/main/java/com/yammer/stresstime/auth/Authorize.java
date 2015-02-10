package com.yammer.stresstime.auth;

import com.yammer.stresstime.auth.Role;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Authorize {
    Role[] value() default {Role.ADMIN, Role.MEMBER, Role.GUEST};
}
