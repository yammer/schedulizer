package com.yammer.stresstime.resources;


import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.*;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/assignments")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentsResource {

    private final AssignmentManager mAssignmentManager;
    private final GroupManager mGroupManager;
    private final EmployeeManager mEmployeeManager;
    private final AssignmentTypeManager mAssignmentTypeManager;
    private final AssignableDayManager mAssignableDayManager;

    public AssignmentsResource(AssignmentManager assignmentManager, GroupManager groupManager,
                               EmployeeManager employeeManager, AssignmentTypeManager assignmentTypeManager,
                               AssignableDayManager assignableDayManager) {
        mAssignmentManager = assignmentManager;
        mGroupManager = groupManager;
        mEmployeeManager = employeeManager;
        mAssignmentTypeManager = assignmentTypeManager;
        mAssignableDayManager = assignableDayManager;
    }

    @GET
    @UnitOfWork
    public Response getListOfAssignments(@QueryParam("group_id") long groupId,
                                         @QueryParam("start_date") String startDateStr,
                                         @QueryParam("end_date") String endDateStr) {
        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date").build();
        }
        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }

        List<AssignableDay> assignableDayList = mAssignableDayManager.getByGroupPeriod(group, startDate, endDate);
        for (AssignableDay assignableDay : assignableDayList) {
            for (Assignment assignment : assignableDay.getAssignments()) {
                assignment.getAssignmentType(); // prevent hibernate's lazy initialization
                assignment.getEmployee();
            }
        }
        return Response.ok().entity(assignableDayList).build();
    }

    @POST
    @UnitOfWork
    public Response createNewAssignment(@FormParam("group_id") long groupId,
                                        @FormParam("employee_id") long employeeId,
                                        @FormParam("assignment_type_id") long assignmentTypeId,
                                        @FormParam("date") String dateStr) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date").build();
        }
        Employee employee = mEmployeeManager.getById(employeeId);
        if (employee == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Employee not found").build();
        }
        AssignmentType assignmentType = mAssignmentTypeManager.getById(assignmentTypeId);
        if (assignmentType == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid assignment type id").build();
        }
        Group group = assignmentType.getGroup();
        if (group.getId() != groupId) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("This assignment does not belong to this group")
                    .build();
        }
        AssignableDay assignableDay = mAssignableDayManager.getByGroupDate(group, date);
        if (assignableDay == null) {
            assignableDay = new AssignableDay(group, date);
            mAssignableDayManager.save(assignableDay);
        }

        Assignment assignment = new Assignment(employee, assignableDay, assignmentType);
        mAssignmentManager.save(assignment);
        return Response.ok().entity(assignment).build();
    }

    @DELETE
    @Path("/{assignment_id}")
    @UnitOfWork
    public Response deleteAssignment(@PathParam("assignment_id") long assignmentId) {
        if (!mAssignmentManager.deleteById(assignmentId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Assignment not found").build();
        }
        return Response.ok().build();
    }
}
