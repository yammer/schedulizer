package com.yammer.stresstime.resources;

import com.yammer.stresstime.auth.Authorize;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import com.yammer.stresstime.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ResourceUtils.checkConflictFree(membership != null, Membership.class);
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
        ResourceUtils.checkConflictFree(membership != null, Membership.class);
        membership.setAdmin(false);
        membershipManager.save(membership);
        return Response.ok().build();
    }
}
