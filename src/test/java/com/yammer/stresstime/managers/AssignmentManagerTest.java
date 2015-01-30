package com.yammer.stresstime.managers;

import com.google.common.base.Throwables;
import com.yammer.stresstime.entities.Assignment;
import com.yammer.stresstime.managers.exceptions.HibernateUncaughtException;
import com.yammer.stresstime.test.DatabaseTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AssignmentManagerTest extends DatabaseTest {

    private AssignmentManager assignmentManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        assignmentManager = new AssignmentManager(getSessionFactory());
    }

    @Test
    public void testAssignmentUniqueness() throws Exception {
        /* TODO: Create different db for tests and make sure there is always an assignment */
        Assignment assignment = assignmentManager.top(1).get(0);
        Assignment clone = new Assignment(
                assignment.getEmployee(),
                assignment.getAssignableDay(),
                assignment.getAssignmentType());

        try {
            assignmentManager.save(clone);
            fail("HibernateUncaughtException was expected but wasn't thrown");
        } catch (HibernateUncaughtException e) {
            assertThat(Throwables.getCausalChain(e), hasItem(instanceOf(ConstraintViolationException.class)));
        }

        hibernateThrewException();
    }
}
