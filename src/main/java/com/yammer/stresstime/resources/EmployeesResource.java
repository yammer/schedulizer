package com.yammer.stresstime.resources;

import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.AssignableDayManager;
import com.yammer.stresstime.managers.AssignmentManager;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeesResource {

    private EmployeeManager employeeManager;
    private AssignmentManager assignmentManager;

    public EmployeesResource(EmployeeManager employeeManager, AssignmentManager assignmentManager) {
        this.employeeManager = employeeManager;
        this.assignmentManager = assignmentManager;
    }

    @GET
    @Path("/{employee_id}/groups")
    @UnitOfWork
    public Response getEmployeeGroups(
            @PathParam("employee_id") long employeeId) {

        Employee employee = employeeManager.getById(employeeId);
        Set<Group> groups = employee.getGroups();
        return Response.ok().entity(groups).build();
    }

    @GET
    @Path("/{employee_id}")
    @UnitOfWork
    public Response getEmployee(
            @PathParam("employee_id") long employeeId) {

        Employee employee = employeeManager.getById(employeeId);
        return Response.ok().entity(employee).build();
    }

    @GET
    @Path("/{employee_id}/assignments")
    @UnitOfWork
    public Response getAssignableDays(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("employee_id") long employeeId,
            @QueryParam("start_date") String startDateString,
            @QueryParam("end_date") String endDateString) {

        Employee employee = employeeManager.getById(employeeId);

        ResourceUtils.checkConflictFree(user.getEmployee().getId() == employeeId, Employee.class);

        ResourceUtils.checkParameter(startDateString != null, "start_date");
        ResourceUtils.checkParameter(endDateString != null, "end_date");

        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        List<Assignment> assignments = assignmentManager.getByEmployeePeriod(employee, startDate, endDate);
        for (Assignment assignment : assignments) {
            assignment.setAnnotationProperty("date", assignment.getAssignableDay().getDateString());
            assignment.setAnnotationProperty("assignmentTypeName", assignment.getAssignmentType().getName());
            assignment.setAnnotationProperty("group", assignment.getAssignmentType().getGroup());
        }

        // Avoid hibernate lazy eval problems with premature session closing
        String response = ResourceUtils.preProcessResponse(assignments);
        return Response.ok().entity(response).build();
    }



    @GET
    @Path("admins")
    @UnitOfWork
    public Response getGlobalAdmins(
            @Authorize({Role.ADMIN}) User user,
            @PathParam("employee_id") long employeeId) {
        return Response.ok().entity(employeeManager.getGlobalAdmins()).build();
    }

    @POST
    @Path("admins")
    @UnitOfWork
    public Response addGlobalAdmin(
            @Authorize({Role.ADMIN}) User user,
            @FormParam("yammerId") String yammerId,
            @FormParam("name") String name,
            @FormParam("imageUrlTemplate") String imageUrlTemplate) {
        Employee employee = employeeManager.getOrCreateByYammerId(yammerId, (Employee e) -> {
            e.setName(name);
            e.setImageUrlTemplate(imageUrlTemplate);
        });
        employee.setGlobalAdmin(true);
        employeeManager.save(employee);
        return Response.ok().entity(employee).build();
    }

    @DELETE
    @Path("admins/{employee_id}")
    @UnitOfWork
    public Response removeGlobalAdmin(
            @Authorize({Role.ADMIN}) User user,
            @PathParam("employee_id") long employeeId) {
        ResourceUtils.checkState(employeeManager.getGlobalAdmins().size() > 1, "You cannot delete the last global admin");
        Employee employee = employeeManager.getById(employeeId);
        employee.setGlobalAdmin(false);
        employeeManager.save(employee);
        return Response.ok().entity(employee).build();
    }
}
