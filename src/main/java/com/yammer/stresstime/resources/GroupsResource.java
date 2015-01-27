package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupsResource {

    private GroupManager groupManager;

    public GroupsResource(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @GET
    @UnitOfWork
    public Response getGroups() {
        List<Group> groups = groupManager.all();
        return Response.ok().entity(groups).build();
    }

    @POST
    @UnitOfWork
    public Response createGroup(@FormParam("name") String name) {
        Group group = new Group(name);
        groupManager.save(group);
        return Response.ok().entity(group).build();
    }

    @DELETE
    @Path("/{group_id}")
    @UnitOfWork
    public Response deleteGroup(@PathParam("group_id") long groupId) {
        groupManager.deleteById(groupId);
        return Response.noContent().build();
    }


    @GET
    @Path("/{group_id}")
    @UnitOfWork
    public Response getGroup(@PathParam("group_id") long groupId) {
        Group group = groupManager.getById(groupId);
        return Response.ok().entity(group).build();
    }

    @POST
    @Path("/test1")
    @UnitOfWork
    public String test2(@FormParam("s")String s) {
        return s;
    }

    @GET
    @Path("/test1")
    @UnitOfWork
    public String test3(@QueryParam("s")String s) {
        return s;
    }
}
