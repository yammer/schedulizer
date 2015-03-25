package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.Membership;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.GroupManager;
import com.yammer.schedulizer.managers.MembershipManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/groups/{group_id}/admins/{employee_id}")
@Produces(MediaType.APPLICATION_JSON)
public class AdminsResource {

    private GroupManager groupManager;
    private MembershipManager membershipManager;

    public AdminsResource(GroupManager groupManager, MembershipManager membershipManager) {
        this.groupManager = groupManager;
        this.membershipManager = membershipManager;
    }

    @POST
    @UnitOfWork
    public Response addAdmin(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("employee_id") long employeeId) {

        Group group = groupManager.getById(groupId);
        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        Membership membership = membershipManager.getByEmployeeIdAndGroupId(employeeId, groupId);
        membership.setAdmin(true);
        membershipManager.save(membership);
        return Response.ok().build();
    }

    @DELETE
    @UnitOfWork
    public Response removeAdmin(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("employee_id") long employeeId) {

        Group group = groupManager.getById(groupId);
        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        Membership membership = membershipManager.getByEmployeeIdAndGroupId(employeeId, groupId);
        membership.setAdmin(false);
        membershipManager.save(membership);
        return Response.ok().build();
    }
}
