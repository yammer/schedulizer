package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.UserManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/migrations")
@Produces(MediaType.APPLICATION_JSON)
public class Migrations {

    UserManager userManager;
    EmployeeManager employeeManager;

    public Migrations(UserManager userManager, EmployeeManager employeeManager) {
        this.userManager = userManager;
        this.employeeManager = employeeManager;
    }

    @GET
    @Path("/extAppId")
    @UnitOfWork
    public Response extAppId() {
        String response = "";
        List<Employee> employees = employeeManager.all();
        for (Employee e : employees) {
            e.setExtAppId(e.getYammerId());
            response += "migrated " + e.getName() + "\n";
        }
        employeeManager.save(employees);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/extApp")
    @UnitOfWork
    public Response extApp() {
        String response = "";
        List<User> users = userManager.all();
        for (User user : users) {
            user.setExtAppType("yammer");
            response += "updated " + user.getEmployee().getName() + "\n";
        }
        userManager.save(users);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/testExtApp")
    @UnitOfWork
    public Response testExtApp() {
        String response = "";
        List<User> users = userManager.all();
        for (User user : users) {
            response += "" + user.getEmployee().getName() +  " is "  + user.getExtAppType() + "\n";
        }
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/testId")
    @UnitOfWork
    public Response testId() {
        String response = "";
        List<Employee> employees = employeeManager.all();
        for (Employee e : employees) {
            response += "" + e.getName() + " has extAppId " + e.getExtAppId() + "\n";
        }
        return Response.ok().entity(response).build();
    }
}
