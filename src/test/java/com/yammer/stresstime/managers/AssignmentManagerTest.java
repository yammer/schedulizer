package com.yammer.stresstime.managers;

import com.google.common.base.Throwables;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.managers.exceptions.HibernateUncaughtException;
import com.yammer.stresstime.test.DatabaseTest;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AssignmentManagerTest extends DatabaseTest {

    private AssignmentManager assignmentManager;
    private GroupManager groupManager;
    private AssignableDayManager assignableDayManager;
    private AssignmentTypeManager assignmentTypeManager;
    private EmployeeManager employeeManager;
    private MembershipManager membershipManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        assignmentManager = new AssignmentManager(getSessionFactory());
        groupManager = new GroupManager(getSessionFactory());
        employeeManager = new EmployeeManager(getSessionFactory());
        membershipManager = new MembershipManager(getSessionFactory());
        assignableDayManager = new AssignableDayManager(getSessionFactory());
        assignmentTypeManager = new AssignmentTypeManager(getSessionFactory());
    }

    @Test
    public void testAssignmentUniqueness() throws Exception {
        Assignment assignment = createAssignment();
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

    private Assignment createAssignment() {
        Group group = new Group("Core Services");
        Employee employee = new Employee("John Doe", "<yid>");
        AssignableDay assignableDay = new AssignableDay(group, new LocalDate(2015, 2, 2));
        AssignmentType assignmentType = new AssignmentType("Primary", group);
        Assignment assignment = new Assignment(employee, assignableDay, assignmentType);
        membershipManager.join(group, employee);
        groupManager.save(group);
        employeeManager.save(employee);
        assignableDayManager.save(assignableDay);
        assignmentTypeManager.save(assignmentType);
        assignmentManager.save(assignment);
        return assignment;
    }
}
