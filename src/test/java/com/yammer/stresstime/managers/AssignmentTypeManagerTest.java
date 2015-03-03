package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.fixtures.AssignmentTypesFixture;
import com.yammer.stresstime.fixtures.GroupsFixture;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.yammer.stresstime.test.TestUtils.assertCauses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
