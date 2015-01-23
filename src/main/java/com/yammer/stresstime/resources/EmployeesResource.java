package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import io.dropwizard.hibernate.UnitOfWork;

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
    public Response joinGroup(
            @PathParam("group_id") long groupId,
            @FormParam("yammerId") String yammerId) {

        Group group = mGroupManager.safeGetById(groupId);
        Employee employee = mEmployeeManager.getOrCreateByYammerId(yammerId);
        mMembershipManager.join(group, employee);
        return Response.ok().entity(employee).build();
    }

    @GET
    @UnitOfWork
    public Response getGroupEmployees(
            @PathParam("group_id") long groupId) {

        Group group = mGroupManager.safeGetById(groupId);
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Group not found").build();
        }
        return Response.ok().entity(group.getEmployees()).build();
    }

    @DELETE
    @Path("{employee_id}")
    @UnitOfWork
    public Response unjoinGroup(
            @PathParam("group_id") long groupId,
            @PathParam("employee_id") long employeeId) {

        Membership membership = mMembershipManager.getByEmployeeIdAndGroupId(employeeId, groupId);
        if (membership == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!mMembershipManager.safeDelete(membership)) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
