package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Assignment;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.AssignmentManager;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/employees/{employee_id}/assignments")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeAssignmentsResource {

    private EmployeeManager employeeManager;
    private AssignmentManager assignmentManager;

    public EmployeeAssignmentsResource(EmployeeManager employeeManager, AssignmentManager assignmentManager) {
        this.employeeManager = employeeManager;
        this.assignmentManager = assignmentManager;
    }

    @GET
    @UnitOfWork
    public Response getAssignments(
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

        return Response.ok().entity(assignments).build();
    }
}
