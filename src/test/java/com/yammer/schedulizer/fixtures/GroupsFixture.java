package com.yammer.schedulizer.fixtures;

import com.google.common.collect.Lists;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.managers.GroupManager;
import org.hibernate.SessionFactory;

import java.util.List;

public class GroupsFixture {

    private List<Group> groups;
    private boolean saved;

    public GroupsFixture() {
        saved = false;
        groups = Lists.newArrayList(new Group("Core Services"), new Group("API"), new Group("IOS"));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        GroupManager groupManager = new GroupManager(sessionFactory);
        groups.stream().forEach(e -> groupManager.save(e));
    }

    public List<Group> getGroups() {
        return groups;
    }
}
