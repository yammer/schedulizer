package com.yammer.stresstime.resources;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.json.AssignmentTypeJSON;
import com.yammer.stresstime.json.ErrorJSON;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/groups/{group_id}/assignment_types")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentTypesResource {
    private final GroupManager mGroupManager;
    AssignmentTypeManager mAssignmentTypeManager;

    public AssignmentTypesResource(AssignmentTypeManager assignmentTypeManager, GroupManager groupManager) {
        mAssignmentTypeManager = assignmentTypeManager;
        mGroupManager = groupManager;
    }

    @POST
    @UnitOfWork
    public Response createNewAssignmentType(@PathParam("group_id") Long groupId,
                                             @FormParam("name") String name,
                                             @FormParam("description") String description) {
        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }
        AssignmentType assignmentType = new AssignmentType();
        assignmentType.setGroup(group);
        assignmentType.setName(name);
        assignmentType.setDescription(description);
        mAssignmentTypeManager.save(assignmentType);
        return Response.ok().entity(new AssignmentTypeJSON(assignmentType)).build();
    }

    @GET
    @UnitOfWork
    public Response getAssignmentTypesFromGroup(@PathParam("group_id") Long groupId) {
        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }

        return Response.ok().entity(Lists.newArrayList(group.getAssignmentTypes())
                        .stream().map(at -> new AssignmentTypeJSON(at)).collect(Collectors.toList())).build();
    }

    @DELETE
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response getGroupsFromEmployee(@PathParam("group_id") Long groupId,
                                          @PathParam("assignment_type_id") Long assignmentTypeId) {
        AssignmentType assignmentType = mAssignmentTypeManager.getById(assignmentTypeId);
        if (assignmentType == null) {
            return Response.status(400).entity(new ErrorJSON("Assignment not found")).build();
        }
        if (assignmentType.getGroup().getId() != groupId) {
            return Response.status(400).entity(new ErrorJSON("Assignment doesn't belong to this group")).build();
        }
        mAssignmentTypeManager.delete(assignmentType);
        return Response.ok().build();
    }
}
