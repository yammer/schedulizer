package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/employees/admins")
@Produces(MediaType.APPLICATION_JSON)
public class GlobalAdminsResource {

    private EmployeeManager employeeManager;
    private ExtAppType extAppType;

    public GlobalAdminsResource(EmployeeManager employeeManager, ExtAppType extAppType) {
        this.employeeManager = employeeManager;
        this.extAppType = extAppType;
    }

    @GET
    @UnitOfWork
    public Response getGlobalAdmins(
            @Authorize({Role.ADMIN}) User user,
            @PathParam("employee_id") long employeeId) {
        List<Employee> globalAdmins = employeeManager.getGlobalAdmins().stream()
                .filter(e -> e.getExtAppType().equals(extAppType))
                .collect(Collectors.toList()); // only show global admins from the same app
        return Response.ok().entity(globalAdmins).build();
    }

    @POST
    @UnitOfWork
    public Response addGlobalAdmin(
            @Authorize({Role.ADMIN}) User user,
            @FormParam("extAppId") String extAppId,
            @FormParam("name") String name,
            @FormParam("imageUrlTemplate") String imageUrlTemplate) {
        Employee employee = employeeManager.getOrCreateByExtAppId(extAppId, extAppType, (Employee e) -> {
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
