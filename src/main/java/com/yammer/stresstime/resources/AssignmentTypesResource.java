package com.yammer.stresstime.resources;

import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.managers.AssignmentTypeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/groups/{group_id}/assignment-types")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentTypesResource {

    private GroupManager groupManager;
    private AssignmentTypeManager assignmentTypeManager;

    public AssignmentTypesResource(
            AssignmentTypeManager assignmentTypeManager,
            GroupManager groupManager) {

        this.assignmentTypeManager = assignmentTypeManager;
        this.groupManager = groupManager;
    }

    @POST
    @UnitOfWork
    public Response createAssignmentType(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @FormParam("name") String name,
            @FormParam("description") String description) {

        Group group = groupManager.getById(groupId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());

        AssignmentType assignmentType = new AssignmentType(name, group);
        assignmentType.setDescription(description);
        assignmentTypeManager.save(assignmentType);
        return Response.ok().entity(assignmentType).build();
    }

    @GET
    @UnitOfWork
    public Response getGroupAssignmentTypes(
            @PathParam("group_id") long groupId) {

        Group group = groupManager.getById(groupId);
        Set<AssignmentType> assignmentTypes = group.getAssignmentTypes();
        return Response.ok().entity(assignmentTypes).build();
    }

    @DELETE
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response deleteAssignmentType(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("assignment_type_id") long assignmentTypeId) {

        AssignmentType assignmentType = assignmentTypeManager.getById(assignmentTypeId);
        Group group = assignmentType.getGroup();

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        ResourceUtils.checkConflictFree(group.getId() == groupId, Group.class);

        assignmentTypeManager.delete(assignmentType);
        return Response.noContent().build();
    }

    @POST
    @Path("/{assignment_type_id}")
    @UnitOfWork
    public Response updateAssignmentType(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("assignment_type_id") long assignmentTypeId,
            @FormParam("name") String name) {

        AssignmentType assignmentType = assignmentTypeManager.getById(assignmentTypeId);
        Group group = assignmentType.getGroup();

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        ResourceUtils.checkConflictFree(group.getId() == groupId, Group.class);

        assignmentType.setName(name);

        assignmentTypeManager.save(assignmentType);
        return Response.ok().entity(assignmentType).build();
    }
}
