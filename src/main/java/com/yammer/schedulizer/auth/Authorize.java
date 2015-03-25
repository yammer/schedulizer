package com.yammer.schedulizer.auth;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Authorize {
    Role[] value() default {Role.ADMIN, Role.MEMBER, Role.GUEST};
}
