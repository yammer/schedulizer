package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/employees/admins")
@Produces(MediaType.APPLICATION_JSON)
public class GlobalAdminsResource {

    private EmployeeManager employeeManager;

    public GlobalAdminsResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    @GET
    @UnitOfWork
    public Response getGlobalAdmins(
            @Authorize({Role.ADMIN}) User user,
            @PathParam("employee_id") long employeeId) {
        return Response.ok().entity(employeeManager.getGlobalAdmins()).build();
    }

    @POST
    @UnitOfWork
    public Response addGlobalAdmin(
            @Authorize({Role.ADMIN}) User user,
            @FormParam("extAppId") String yammerId,
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
    @Path("/{employee_id}")
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
