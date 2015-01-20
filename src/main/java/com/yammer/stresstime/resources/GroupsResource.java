package com.yammer.stresstime.resources;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.json.GroupJSON;
import com.yammer.stresstime.json.ErrorJSON;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.constraints.NotNull;
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
    public List<GroupJSON> getAllGroups() {
        List<Group> groups = mGroupManager.all();
        List<GroupJSON> groupJSONs = Lists.transform(groups, g -> new GroupJSON(g));
        return groupJSONs;
    }

    @POST
    @UnitOfWork
    public GroupJSON createGroup(@FormParam("name") String name) {
        Group group = new Group();
        group.setName(name);
        mGroupManager.save(group);
        return new GroupJSON(group);
    }

    @DELETE
    @UnitOfWork
    public Response deleteGroup(@FormParam("id") Long id) {
        if(!mGroupManager.deleteById(id)) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }
        return Response.ok().build();
    }


    @GET
    @Path("/{group_id}")
    @UnitOfWork
    public Response getGroupById(@PathParam("group_id") Long id) {
        Group group = mGroupManager.getById(id);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }
        return Response.ok().entity(new GroupJSON(group)).build();
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
