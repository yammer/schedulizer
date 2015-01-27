package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/groups/{group_id}/assignment_types")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentTypesResource {
    private GroupManager mGroupManager;
    private AssignmentTypeManager mAssignmentTypeManager;

    public AssignmentTypesResource(AssignmentTypeManager assignmentTypeManager, GroupManager groupManager) {
        mAssignmentTypeManager = assignmentTypeManager;
        mGroupManager = groupManager;
    }

    @POST
    @UnitOfWork
    public Response createAssignmentType(
            @PathParam("group_id") long groupId,
            @FormParam("name") String name,
            @FormParam("description") String description) {

        Group group = mGroupManager.getById(groupId);
        AssignmentType assignmentType = new AssignmentType(name, group);
        assignmentType.setDescription(description);
        mAssignmentTypeManager.save(assignmentType);
        return Response.ok().entity(assignmentType).build();
    }

    @GET
    @UnitOfWork
    public Response getGroupAssignmentTypes(@PathParam("group_id") long groupId) {
        Group group = mGroupManager.getById(groupId);
        Set<AssignmentType> assignmentTypes = group.getAssignmentTypes();
        return Response.ok().entity(assignmentTypes).build();
    }

    @DELETE
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response deleteAssignmentType(
            @PathParam("group_id") long groupId,
            @PathParam("assignment_type_id") long assignmentTypeId) {

        AssignmentType assignmentType = mAssignmentTypeManager.getById(assignmentTypeId);
        ResourceUtils.checkConflictFree(assignmentType.getGroup().getId() == groupId, Group.class);
        mAssignmentTypeManager.delete(assignmentType);
        return Response.noContent().build();
    }
}
