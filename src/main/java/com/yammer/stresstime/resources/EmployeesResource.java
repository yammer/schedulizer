package com.yammer.stresstime.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import io.dropwizard.hibernate.UnitOfWork;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

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
    public Response addEmployeeToGroup(@PathParam("group_id") long groupId,
                                       @FormParam("yid") String yammerId) {
        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
        Employee employee = mEmployeeManager.getByYammerId(yammerId);
        if (employee == null) {
            // new yammer id
            employee = mEmployeeManager.createNewEmployee(yammerId);
        }

        Membership membership = mMembershipManager.getByEmployeeAndGroup(employee.getId(), groupId);
        if (membership == null) {
            membership = new Membership(employee, group);
            membership.setAdmin(false);
            mMembershipManager.save(membership);
        }
        return Response.ok().entity(employee).build();
    }

    @GET
    @UnitOfWork
    public Response showEmployeesFromGroup(@PathParam("group_id") long groupId) {

        Group group = mGroupManager.getById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
        return Response.ok().entity(group.getEmployees()).build();
    }

    @DELETE
    @Path("{employee_id}")
    @UnitOfWork
    public Response addEmployeeToGroup(@PathParam("group_id") long groupId,
                                       @PathParam("employee_id") long employeeId) {
        if (!mMembershipManager.deleteByEmployeeAndGroup(employeeId, groupId)) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("This employee does not belong to this group")
                    .build();
        }
        return Response.ok().build();
    }
}
