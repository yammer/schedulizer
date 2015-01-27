package com.yammer.stresstime.resources;


import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.*;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/groups/{group_id}/assignments")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentsResource {

    private AssignmentManager assignmentManager;
    private GroupManager groupManager;
    private EmployeeManager employeeManager;
    private AssignmentTypeManager assignmentTypeManager;
    private AssignableDayManager assignableDayManager;

    public AssignmentsResource(
            AssignmentManager assignmentManager,
            GroupManager groupManager,
            EmployeeManager employeeManager,
            AssignmentTypeManager assignmentTypeManager,
            AssignableDayManager assignableDayManager) {

        this.assignmentManager = assignmentManager;
        this.groupManager = groupManager;
        this.employeeManager = employeeManager;
        this.assignmentTypeManager = assignmentTypeManager;
        this.assignableDayManager = assignableDayManager;
    }

    @GET
    @UnitOfWork
    public Response getAssignableDays(
            @PathParam("group_id") long groupId,
            @QueryParam("start_date") String startDateString,
            @QueryParam("end_date") String endDateString) {

        ResourceUtils.checkParameter(startDateString != null, "start_date");
        ResourceUtils.checkParameter(endDateString != null, "end_date");

        Group group = groupManager.getById(groupId);
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        List<AssignableDay> assignableDays = assignableDayManager.getByGroupPeriod(group, startDate, endDate);

        // TODO: Testst
        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(assignableDays);
        return Response.ok().entity(response).build();
    }

    @POST
    @UnitOfWork
    public Response createAssignment(
            @PathParam("group_id") long groupId,
            @FormParam("employee_id") long employeeId,
            @FormParam("assignment_type_id") long assignmentTypeId,
            @FormParam("date") String dateString) {

        ResourceUtils.checkParameter(dateString != null, "date");

        LocalDate date = LocalDate.parse(dateString);
        Employee employee = employeeManager.getById(employeeId);
        AssignmentType assignmentType = assignmentTypeManager.getById(assignmentTypeId);
        Group group = assignmentType.getGroup();
        ResourceUtils.checkConflictFree(group.getId() == groupId, Group.class);
        AssignableDay assignableDay = assignableDayManager.getOrCreateByGroupDate(group, date);
        Assignment assignment = new Assignment(employee, assignableDay, assignmentType);
        assignmentManager.save(assignment);
        return Response.ok().entity(assignment).build();
    }

    @DELETE
    @Path("/{assignment_id}")
    @UnitOfWork
    public Response deleteAssignment(
            @PathParam("group_id") long groupId,
            @PathParam("assignment_id") long assignmentId) {

        Assignment assignment = assignmentManager.getById(assignmentId);
        ResourceUtils.checkConflictFree(assignment.getAssignableDay().getGroup().getId() == groupId, Group.class);
        assignmentManager.delete(assignment);
        return Response.noContent().build();
    }
}
