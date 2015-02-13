package com.yammer.stresstime.resources;

import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
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
