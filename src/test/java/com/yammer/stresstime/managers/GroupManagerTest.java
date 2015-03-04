package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.fixtures.GroupsFixture;

import java.util.List;

public class GroupManagerTest extends BaseManagerTest<Group> {

    private GroupManager groupManager;
    private List<Group> testGroups;

    @Override
    protected EntityManager<Group> getEntityManager() {
        return groupManager;
    }

    @Override
    protected List<Group> getEntities() {
        return testGroups;
    }

    @Override
    protected void initialize() {
        groupManager = new GroupManager(getSessionFactory());
        GroupsFixture groupsFixture = new GroupsFixture();
        testGroups = groupsFixture.getGroups();
    }

    @Override
    protected void clean() {}
}
