package com.yammer.stresstime.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.json.EmployeeJSON;
import com.yammer.stresstime.json.ErrorJSON;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import io.dropwizard.hibernate.UnitOfWork;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/groups/{group_id}/employees")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeesResource {

    private EmployeeManager mEmployeeManager;
    private GroupManager mGroupManager;
    private MembershipManager mMembershipManager;

    public EmployeesResource(EmployeeManager employeeManager,
                             GroupManager groupManager,
                             MembershipManager membershipManager) {
        mEmployeeManager = employeeManager;
        mGroupManager = groupManager;
        mMembershipManager = membershipManager;
    }

    @POST
    @UnitOfWork
    public Response addEmployeeToGroup(@PathParam("group_id") Long group_id,
                                       @FormParam("yid") String yammer_id) {
        Group group = mGroupManager.getById(group_id);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }
        Employee employee = mEmployeeManager.getByYammerId(yammer_id);
        if (employee == null) {
            // new yammer id
            employee = mEmployeeManager.createNewEmployee(yammer_id);
        }

        Membership membership = mMembershipManager.getByEmployeeAndGroup(employee.getId(), group_id);
        if (membership == null) {
            membership = new Membership();
            membership.setEmployee(employee);
            membership.setGroup(group);
            membership.setAdmin(false);
            mMembershipManager.save(membership);
        }
        return Response.ok().entity(new EmployeeJSON(employee)).build();
    }

    @GET
    @UnitOfWork
    public Response showEmployeesFromGroup(@PathParam("group_id") Long group_id) {

        Group group = mGroupManager.getById(group_id);
        if (group == null) {
            return Response.status(400).entity(new ErrorJSON("Group not found")).build();
        }
        return Response.ok().entity(
                Lists.transform(Lists.newArrayList(group.getEmployees()), e -> new EmployeeJSON(e))).build();
    }

    @DELETE
    @Path("{employee_id}")
    @UnitOfWork
    public Response addEmployeeToGroup(@PathParam("group_id") Long group_id,
                                       @PathParam("employee_id") Long employee_id) {
        if (!mMembershipManager.deleteByEmployeeAndGroup(employee_id, group_id)) {
            return Response.status(400).entity(new ErrorJSON("This employee does not belong to this group")).build();
        }
        return Response.ok().build();
    }
}
