package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response createNewAssignmentType(@PathParam("group_id") long groupId,
                                             @FormParam("name") String name,
                                             @FormParam("description") String description) {
        Group group = mGroupManager.safeGetById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
        AssignmentType assignmentType = new AssignmentType(name, group);
        assignmentType.setDescription(description);
        mAssignmentTypeManager.save(assignmentType);
        return Response.ok().entity(assignmentType).build();
    }

    @GET
    @UnitOfWork
    public Response getAssignmentTypesFromGroup(@PathParam("group_id") long groupId) {
        Group group = mGroupManager.safeGetById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }

        return Response.ok().entity(group.getAssignmentTypes()).build();
    }

    @DELETE
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response getGroupsFromEmployee(@PathParam("group_id") long groupId,
                                          @PathParam("assignment_type_id") long assignmentTypeId) {
        AssignmentType assignmentType = mAssignmentTypeManager.safeGetById(assignmentTypeId);
        if (assignmentType == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Assignment not found").build();
        }
        if (assignmentType.getGroup().getId() != groupId) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Assignment doesn't belong to this group")
                    .build();
        }
        mAssignmentTypeManager.delete(assignmentType);
        return Response.ok().build();
    }
}
