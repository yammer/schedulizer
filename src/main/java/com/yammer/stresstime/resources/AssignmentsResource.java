package com.yammer.stresstime.resources;


import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.json.AssignmentJSON;
import com.yammer.stresstime.json.AssignableDayJSON;
import com.yammer.stresstime.json.ErrorJSON;
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
    public Response getListOfAssignments(@QueryParam("group_id") Long groupId,
                                         @QueryParam("start_date") String startDateStr,
                                         @QueryParam("end_date") String endDateStr) {
        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        }
        catch (IllegalArgumentException e) {
            return Response.status(400).entity(new ErrorJSON("Invalid date")).build();
        }
        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }

        List<AssignableDay> assignableDayList = mAssignableDayManager.getByGroupPeriod(group, startDate, endDate);
        return Response.ok().entity(
                assignableDayList.stream().map(ad -> new AssignableDayJSON(ad)).collect(Collectors.toList())).build();
    }

    @POST
    @UnitOfWork
    public Response createNewAssignment(@FormParam("group_id") Long groupId,
                                        @FormParam("employee_id") Long employeeId,
                                        @FormParam("assignment_type_id") Long assignmentTypeId,
                                        @FormParam("date") String dateStr) {
        LocalDate date;
        System.out.println(dateStr);
        try {
            date = LocalDate.parse(dateStr);
        }
        catch (IllegalArgumentException e) {
            return Response.status(400).entity(new ErrorJSON("Invalid date")).build();
        }
        Employee employee = mEmployeeManager.getById(employeeId);
        if (employee == null) {
            return Response.status(400).entity(new ErrorJSON("Employee not found")).build();
        }
        AssignmentType assignmentType = mAssignmentTypeManager.getById(assignmentTypeId);
        if (assignmentType == null) {
            return Response.status(400).entity(new ErrorJSON("Invalid assignment type id")).build();
        }
        Group group = assignmentType.getGroup();
        if (group.getId() != groupId) {
            return Response.status(400).entity(new ErrorJSON("This assignment does not belong to this group")).build();
        }
        AssignableDay assignableDay = mAssignableDayManager.getByGroupDate(group, date);
        if (assignableDay == null) {
            assignableDay = new AssignableDay();
            assignableDay.setGroup(group);
            assignableDay.setDate(date);
            assignableDay = mAssignableDayManager.save(assignableDay);
        }

        Assignment assignment = new Assignment();
        assignment.setEmployee(employee);
        assignment.setAssignableDay(assignableDay);
        assignment.setAssignmentType(assignmentType);
        mAssignmentManager.save(assignment);
        return Response.ok().entity(new AssignmentJSON(assignment)).build();
    }

    @DELETE
    @Path("/{assignment_id}")
    @UnitOfWork
    public Response deleteAssignment(@PathParam("assignment_id") Long assignmentId) {
        if (!mAssignmentManager.deleteById(assignmentId)) {
            return Response.status(400).entity(new ErrorJSON("Assignment not found")).build();
        }
        return Response.ok().build();
    }
}
