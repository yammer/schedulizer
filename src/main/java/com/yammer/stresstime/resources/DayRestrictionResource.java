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

@Path("/employees/{employee_id}/restrictions")
@Produces(MediaType.APPLICATION_JSON)
public class DayRestrictionResource {

    private EmployeeManager employeeManager;
    private DayRestrictionManager dayRestrictionManager;

    public DayRestrictionResource(EmployeeManager employeeManager, DayRestrictionManager dayRestrictionManager) {
        this.employeeManager = employeeManager;
        this.dayRestrictionManager = dayRestrictionManager;
    }

    @GET
    @UnitOfWork
    public Response getDayRestrictions(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("employee_id") long employeeId,
            @QueryParam("start_date") String startDateString,
            @QueryParam("end_date") String endDateString) {

        Employee employee = employeeManager.getById(employeeId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(employee.getGroups(), user.getEmployee());
        ResourceUtils.checkParameter(startDateString != null, "start_date");
        ResourceUtils.checkParameter(endDateString != null, "end_date");

        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        List<DayRestriction> dayRestrictions = dayRestrictionManager.getByEmployeePeriod(employee, startDate, endDate);

        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(dayRestrictions);
        return Response.ok().entity(response).build();
    }

    @POST
    @UnitOfWork
    public Response createDayRestriction(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("employee_id") long employeeId,
            @FormParam("dates") String dates,
            @FormParam("comment") String comment,
            @FormParam("restriction_level") int restrictionLevel) {

        Employee employee = employeeManager.getById(employeeId);

        ResourceUtils.checkSameEmployee(employee, user.getEmployee());
        ResourceUtils.checkParameter(dates != null, "dates");

        List<DayRestriction> dayRestrictions = Arrays.stream(dates.split(","))
                .map(LocalDate::parse)
                .map(d -> dayRestrictionManager.getOrCreateByEmployeeDateComment(employee, d, comment, restrictionLevel))
                .collect(Collectors.toList());

        dayRestrictionManager.save(dayRestrictions);

        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(dayRestrictions);
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/{day_restriction_id}")
    @UnitOfWork
    public Response deleteDayRestriction(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("employee_id") long employeeId,
            @PathParam("day_restriction_id") long dayRestrictionId) {

        DayRestriction dayRestriction = dayRestrictionManager.getById(dayRestrictionId);
        Employee employee = dayRestriction.getEmployee();

        ResourceUtils.checkConflictFree(employee.getId() == employeeId, Employee.class);
        ResourceUtils.checkSameEmployee(employee, user.getEmployee());

        dayRestrictionManager.delete(dayRestriction);
        return Response.ok().build();
    }
}
