package com.yammer.stresstime.resources;


import com.google.common.collect.ImmutableMap;
import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.*;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(assignableDays);
        return Response.ok().entity(response).build();
    }

    @POST
    @UnitOfWork
    public Response createAssignment(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @FormParam("employee_id") long employeeId,
            @FormParam("assignment_type_id") long assignmentTypeId,
            @FormParam("dates") String dates) {

        Employee employee = employeeManager.getById(employeeId);
        AssignmentType assignmentType = assignmentTypeManager.getById(assignmentTypeId);
        Group group = assignmentType.getGroup();

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        ResourceUtils.checkConflictFree(group.getId() == groupId, Group.class);
        ResourceUtils.checkParameter(dates != null, "dates");

        List<AssignableDay> assignableDays = Arrays.stream(dates.split(","))
                .map(LocalDate::parse)
                .map(d -> assignableDayManager.getOrCreateByGroupAndDate(group, d))
                .collect(Collectors.toList());

        List<Assignment> assignments = assignableDays.stream()
                .map(a -> new Assignment(employee, a, assignmentType))
                .filter(a -> !assignmentManager.exists(a))
                .collect(Collectors.toList());

        assignmentManager.save(assignments);
        assignableDayManager.refresh(assignableDays);

        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(assignableDays);
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/{assignment_id}")
    @UnitOfWork
    public Response deleteAssignment(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("assignment_id") long assignmentId) {

        Assignment assignment = assignmentManager.getById(assignmentId);
        Group group = assignment.getAssignableDay().getGroup();

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        ResourceUtils.checkConflictFree(group.getId() == groupId, Group.class);

        AssignableDay assignableDay = assignment.getAssignableDay();
        assignmentManager.delete(assignment);
        assignableDayManager.refresh(assignableDay);
        String response = ResourceUtils.preProcessResponse(assignableDay);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/stats")
    @UnitOfWork
    public Response getAssignmentsStats(
            @PathParam("group_id") long groupId,
            @QueryParam("start_date") String startDateString,
            @QueryParam("end_date") String endDateString) {

        ResourceUtils.checkParameter(startDateString != null, "start_date");
        ResourceUtils.checkParameter(endDateString != null, "end_date");
        Group group = groupManager.getById(groupId);
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        List<AssignableDay> assignableDays = assignableDayManager.getByGroupPeriod(group, startDate, endDate);
        Map<Employee, Map<AssignmentType, Long>> statistics = AssignableDayManager.getStatistics(assignableDays);

        Map<Long, Set<Map<String, Long>>> response = processStatisticsResponse(statistics);
        return Response.ok().entity(response).build();
    }

    /**
     * @return {(Long) employeeId => [{"assignmentTypeId" => Long, "count" => Long}] }
     */
    private Map<Long, Set<Map<String, Long>>> processStatisticsResponse(
            Map<Employee, Map<AssignmentType, Long>> statistics) {

        return statistics
                .entrySet().stream()
                .collect(Collectors.toMap(
                        (Map.Entry<Employee, Map<AssignmentType, Long>> e) -> e.getKey().getId(),
                        (Map.Entry<Employee, Map<AssignmentType, Long>> e) -> e.getValue()
                                .entrySet().stream()
                                .map(c -> (Map<String, Long>) ImmutableMap.<String, Long>builder()
                                        .put("assignmentTypeId", c.getKey().getId())
                                        .put("count", c.getValue())
                                        .build())
                                .collect(Collectors.toSet())));
    }
}
