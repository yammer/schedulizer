package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.managers.EmployeeManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/migrations")
@Produces(MediaType.APPLICATION_JSON)
public class Migrations {
    EmployeeManager employeeManager;
    public Migrations(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }
    @GET
    @Path("/extAppType")
    @UnitOfWork
    public String migrate(){
        List<Employee> employees = employeeManager.all();
        String response = "";
        for (Employee employee : employees) {
            employee.setExtAppType("yammer");
            response += "migrated" + employee.getName() +  "\n";
        }
        employeeManager.save(employees);
        return response;
    }


    @GET
    @Path("/test")
    @UnitOfWork
    public String test(){
        List<Employee> employees = employeeManager.all();
        String response = "";
        for (Employee employee : employees) {
            response += employee.getName() + " is " + employee.getExtAppType() + "\n";
        }
        return response;
    }
}
