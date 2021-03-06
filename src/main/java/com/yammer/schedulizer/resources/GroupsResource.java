package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.GroupManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupsResource {

    private GroupManager groupManager;

    public GroupsResource(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @GET
    @UnitOfWork
    public Response getGroups(
            @Authorize({Role.ADMIN, Role.MEMBER, Role.GUEST}) User user) {

        List<Group> groups = groupManager.all();
        List<Group> response = wrapGroups(groups, user);
        return Response.ok().entity(response).build();
    }

    @POST
    @UnitOfWork
    public Response createGroup(
            @Authorize({Role.ADMIN}) User user,
            @FormParam("name") String name) {

        Group group = new Group(name);
        groupManager.save(group);
        Group response = wrapGroup(group, user);
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/{group_id}")
    @UnitOfWork
    public Response deleteGroup(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId) {

        Group group = groupManager.getById(groupId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());

        groupManager.delete(group);
        return Response.noContent().build();
    }

    @POST
    @Path("/{group_id}")
    @UnitOfWork
    public Response updateGroup(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @FormParam("name") String name) {

        Group group = groupManager.getById(groupId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        group.setName(name);
        groupManager.save(group);
        return Response.ok().entity(group).build();
    }


    @GET
    @Path("/{group_id}")
    @UnitOfWork
    public Response getGroup(
            @Authorize({Role.ADMIN, Role.MEMBER, Role.GUEST}) User user,
            @PathParam("group_id") long groupId) {

        Group group = groupManager.getById(groupId);
        Group response = wrapGroup(group, user);
        return Response.ok().entity(response).build();
    }

    private Group wrapGroup(Group group, User user) {
        Employee employee = user.getEmployee();
        group.setAnnotationProperty("isMember", employee != null && group.isMember(employee));
        group.setAnnotationProperty("isAdmin", employee != null && group.isAdmin(employee));
        return group;
    }

    private List<Group> wrapGroups(Collection<? extends Group> groups, User user) {
        return groups.stream()
                .map(g -> wrapGroup(g, user))
                .collect(Collectors.toList());
    }
}
