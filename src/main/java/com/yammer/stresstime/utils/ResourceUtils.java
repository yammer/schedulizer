package com.yammer.stresstime.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.exceptions.DataConflictException;
import com.yammer.stresstime.managers.exceptions.InvalidStateException;
import com.yammer.stresstime.managers.exceptions.ParameterException;
import com.yammer.stresstime.managers.exceptions.UnauthorizedAccessException;
import io.dropwizard.jackson.Jackson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ResourceUtils {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

    // Method used in cases where you need to pre-process the response before returning from a resource
    // method, most probably b/c of lazy db evaluation from hibernate that will happen after the
    // session was closed. (dropwizard-hibernate closes the session before the response is processed)
    // Avoid using this method b/c it has been created with Jersey implementation in mind, and it
    // doesn't do any housekeeping tasks as Jersey does, like handling/propagating exceptions properly.
    public static <E> String preProcessResponse(E response) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            OBJECT_MAPPER.writeValue(out, response);
            // Jackson writes in UTF-8 by default
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // Swallow, we are closing
            }
        }
    }

    public static void checkConflictFree(boolean condition, Class<?> klass) {
        if (!condition) {
            throw new DataConflictException(klass);
        }
    }

    public static void checkParameter(boolean condition, String name) {
        if (!condition) {
            throw new ParameterException(name);
        }
    }

    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new InvalidStateException(message);
        }
    }

    public static void checkGroupAdminOrGlobalAdmin(Group group, Employee employee) {
        if (!employee.isGlobalAdmin() && !group.isAdmin(employee)) {
            throw new UnauthorizedAccessException();
        }
    }

    public static void checkGroupAdminOrGlobalAdmin(Collection<? extends Group> groups, Employee employee) {
        if (!employee.isGlobalAdmin() && !groups.stream().anyMatch(g -> g.isAdmin(employee))) {
            throw new UnauthorizedAccessException();
        }
    }

    public static void checkGroupMemberOrGlobalAdmin(Group group, Employee employee) {
        if (!employee.isGlobalAdmin() && !group.isMember(employee)) {
            throw new UnauthorizedAccessException();
        }
    }

    // Prevents instantiation
    private ResourceUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
