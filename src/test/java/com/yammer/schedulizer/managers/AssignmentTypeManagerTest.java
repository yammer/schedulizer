package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.AssignmentType;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.fixtures.AssignmentTypesFixture;
import com.yammer.schedulizer.fixtures.GroupsFixture;

import java.util.List;

public class AssignmentTypeManagerTest extends BaseManagerTest<AssignmentType> {

    private AssignmentTypeManager assignmentTypeManager;
    private List<AssignmentType> testAssignmentTypes;
    private List<Group> groups;

    @Override
    protected EntityManager<AssignmentType> getEntityManager() {
        return assignmentTypeManager;
    }

    @Override
    protected List<AssignmentType> getEntities() {
        return testAssignmentTypes;
    }

    @Override
    protected void initialize() {
        assignmentTypeManager = new AssignmentTypeManager(getSessionFactory());
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        groups = groupsFixture.getGroups();
        AssignmentTypesFixture assignmentTypesFixture = new AssignmentTypesFixture(groupsFixture);
        testAssignmentTypes = assignmentTypesFixture.getAssignmentTypes();
    }

    @Override
    protected void clean() {}
}
