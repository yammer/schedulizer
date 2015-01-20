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
    public Response createNewAssignementType(@PathParam("group_id") Long group_id,
                                             @FormParam("name") String name,
                                             @FormParam("description") String description) {
        Group group = mGroupManager.getById(group_id);
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
    public Response getAssignmentTypesFromGroup(@PathParam("group_id") Long group_id) {
        Group group = mGroupManager.getById(group_id);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }

        return Response.ok().entity(
                Lists.transform(Lists.newArrayList(group.getAssignmentTypes()), at -> new AssignmentTypeJSON(at))
        ).build();
    }

    @DELETE
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response getGroupsFromEmployee(@PathParam("group_id") Long group_id,
                                          @PathParam("assignment_type_id") Long assignment_type_id) {
        AssignmentType assignmentType = mAssignmentTypeManager.getById(assignment_type_id);
        if (assignmentType == null) {
            return Response.status(400).entity(new ErrorJSON("Assignment not found")).build();
        }
        if (assignmentType.getGroup().getId() != group_id) {
            return Response.status(400).entity(new ErrorJSON("Assignment doesn't belong to this group")).build();
        }
        mAssignmentTypeManager.delete(assignmentType);
        return Response.ok().build();
    }
}
