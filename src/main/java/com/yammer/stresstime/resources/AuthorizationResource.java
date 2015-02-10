package com.yammer.stresstime.resources;

import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.entities.User;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/current")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

    @GET
    @UnitOfWork
    public Response getCurrent(
            @Authorize({Role.ADMIN, Role.MEMBER, Role.GUEST}) User user) {

        Map<String, Object> response = new HashMap<>();
        response.put("role", user.getRole().toString());
        if (user.getEmployee() != null) {
            response.put("employeeId", user.getEmployee().getId());
            List<Long> groupIds = user.getEmployee().getMemberships().stream()
                    .filter(m -> m.isAdmin())
                    .map(Membership::getGroup)
                    .map(Group::getId)
                    .collect(Collectors.toList());
            response.put("groupsAdmin", groupIds);
        }

        return Response.ok().entity(response).build();
    }
}
