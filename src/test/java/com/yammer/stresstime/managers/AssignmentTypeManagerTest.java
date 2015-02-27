package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.User;
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
        groups = Lists.newArrayList(new Group("group 1"), new Group("group 2"), new Group("group 3"));
        GroupManager groupManager = new GroupManager(getSessionFactory());
        groups.stream().forEach(g -> groupManager.save(g));
        testAssignmentTypes = Lists.newArrayList(new AssignmentType("Primary", groups.get(0)),
                new AssignmentType("Secondary", groups.get(0)),
                new AssignmentType("Primary Support", groups.get(0)),
                new AssignmentType("AT 1", groups.get(1)),
                new AssignmentType("AT 2", groups.get(1)),
                new AssignmentType("AT 3", groups.get(1)),
                new AssignmentType("AT 4", groups.get(1)),
                new AssignmentType("Name", groups.get(2)));
    }

    @Override
    protected void clean() {}
}
