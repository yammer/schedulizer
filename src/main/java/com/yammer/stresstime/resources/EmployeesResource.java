package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.EmployeeManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeesResource {

    private EmployeeManager employeeManager;

    public EmployeesResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
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
}
