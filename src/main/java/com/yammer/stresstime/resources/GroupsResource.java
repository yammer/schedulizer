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

    private GroupManager mGroupManager;

    public GroupsResource(GroupManager groupManager) {
        mGroupManager = groupManager;
    }

    @GET
    @UnitOfWork
    public Response getAllGroups() {
        List<Group> groups = mGroupManager.all();
        return Response.ok().entity(groups).build();
    }

    @POST
    @UnitOfWork
    public Response createGroup(@FormParam("name") String name) {
        Group group = new Group(name);
        mGroupManager.save(group);
        return Response.ok().entity(group).build();
    }

    @DELETE
    @Path("/{group_id}")
    @UnitOfWork
    public Response deleteGroup(@PathParam("group_id") long groupId) {
        if (!mGroupManager.safeDeleteById(groupId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
        return Response.ok().build();
    }


    @GET
    @Path("/{group_id}")
    @UnitOfWork
    public Response getGroupById(@PathParam("group_id") long groupId) {
        Group group = mGroupManager.safeGetById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
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
