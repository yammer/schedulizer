package com.yammer.stresstime.resources;

import com.google.common.base.Optional;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.managers.GroupManager;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("/test")
public class TestResource {

    private final GroupManager mGroupManager;

    public TestResource(GroupManager groupManager) {
        mGroupManager = groupManager;
    }

    @GET
    @UnitOfWork
    public String test() {
        List<Group> groups = mGroupManager.all();
        StringBuilder ans = new StringBuilder();
        for (Group group : groups) {
            ans.append(group.getId()).append(": ").append(group.getName()).append("<br />\n");
        }
        ans.append("<br />\n").append("Total of ").append(groups.size()).append(" groups");
        return ans.toString();
    }

    @GET
    @Path("/1")
    @UnitOfWork
    public String test1(@QueryParam("name") Optional<String> name) {
        Group group = new Group();
        group.setName(name.or("Core Services - SF"));
        mGroupManager.save(group);
        return "done";
    }

    @GET
    @Path("/2")
    @UnitOfWork
    public String test2() {
        Group group = new Group();
        return "done";
    }
}
