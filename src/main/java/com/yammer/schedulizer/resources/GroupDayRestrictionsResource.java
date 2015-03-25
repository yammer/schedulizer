package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.auth.Authorize;
import com.yammer.schedulizer.auth.Role;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.managers.DayRestrictionManager;
import com.yammer.schedulizer.managers.GroupManager;
import com.yammer.schedulizer.utils.ResourceUtils;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/groups/{group_id}/restrictions")
@Produces(MediaType.APPLICATION_JSON)
public class GroupDayRestrictionsResource {

    private GroupManager groupManager;
    private DayRestrictionManager dayRestrictionManager;

    public GroupDayRestrictionsResource(GroupManager groupManager, DayRestrictionManager dayRestrictionManager) {
        this.groupManager = groupManager;
        this.dayRestrictionManager = dayRestrictionManager;
    }

    @GET
    @UnitOfWork
    public Response getGroupRestrictions(
            @Authorize({Role.ADMIN, Role.MEMBER}) User user,
            @PathParam("group_id") long groupId,
            @QueryParam("start_date") String startDateString,
            @QueryParam("end_date") String endDateString) {

        Group group = groupManager.getById(groupId);
        ResourceUtils.checkGroupAdminOrGlobalAdmin(group, user.getEmployee());
        ResourceUtils.checkParameter(startDateString != null, "start_date");
        ResourceUtils.checkParameter(endDateString != null, "end_date");

        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);

        return Response.ok().entity(dayRestrictionManager.getByGroupPeriod(group, startDate, endDate)).build();
    }
}
