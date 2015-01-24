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

@Path("/employees/{employee_id}/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupEmployeesResource {

    EmployeeManager mEmployeeManager;

    public GroupEmployeesResource(EmployeeManager employeeManager) {
        mEmployeeManager = employeeManager;
    }

    @GET
    @UnitOfWork
    public Response getEmployeeGroups(@PathParam("employee_id") long employeeId) {
        Employee employee = mEmployeeManager.getById(employeeId);
        Set<Group> groups = employee.getGroups();
        return Response.ok().entity(groups).build();
    }
}
