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
import java.util.Set;

@Path("/groups/{group_id}/employees")
@Produces(MediaType.APPLICATION_JSON)
public class GroupEmployeesResource {

    private EmployeeManager employeeManager;
    private GroupManager groupManager;
    private MembershipManager membershipManager;

    public GroupEmployeesResource(
            EmployeeManager employeeManager,
            GroupManager groupManager,
            MembershipManager membershipManager) {

        this.employeeManager = employeeManager;
        this.groupManager = groupManager;
        this.membershipManager = membershipManager;
    }

    @POST
    @UnitOfWork
    public Response joinGroup(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @FormParam("yammerId") String yammerId,
            @FormParam("name") String name,
            @FormParam("imageUrlTemplate") String imageUrlTemplate) {

        Group group = groupManager.getById(groupId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());

        Employee employee = employeeManager.getOrCreateByYammerId(yammerId, (Employee e) -> {
            e.setName(name);
            e.setImageUrlTemplate(imageUrlTemplate);
        });
        membershipManager.join(group, employee);
        return Response.ok().entity(employee).build();
    }

    @GET
    @UnitOfWork
    public Response getGroupEmployees(
            @PathParam("group_id") long groupId) {

        Group group = groupManager.getById(groupId);
        Set<Employee> employees = group.getEmployees();
        for (Employee employee : employees) {
            employee.setAnnotationProperty("groupAdmin", group.isAdmin(employee));
        }
        return Response.ok().entity(employees).build();
    }

    @DELETE
    @Path("{employee_id}")
    @UnitOfWork
    public Response unjoinGroup(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @PathParam("employee_id") long employeeId) {

        Group group = groupManager.getById(groupId);

        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());

        Membership membership = membershipManager.getByEmployeeIdAndGroupId(employeeId, groupId);
        membershipManager.delete(membership);
        return Response.noContent().build();
    }
}
