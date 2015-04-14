package com.yammer.schedulizer.entities;

import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.managers.*;
import com.yammer.schedulizer.test.DatabaseTest;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.schedulizer.test.TestUtils.assertCauses;

public class AssignmentTest extends DatabaseTest {

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

        assertCauses(ConstraintViolationException.class, () -> assignmentManager.save(clone));

        hibernateThrewException();
    }

    @Test
    public void testAssignableDayAndAssignmentTypeBelongToSameGroup() {
        Assignment assignment = createAssignment();
        Group otherGroup = new Group("group");
        groupManager.save(otherGroup);

        // Must not throw
        AssignableDay assignableDay =
                new AssignableDay(assignment.getAssignableDay().getGroup(), new LocalDate(2015,10,10));
        assignableDayManager.save(assignableDay);
        assignment.setAssignableDay(assignableDay);
        // Must not throw
        AssignmentType assignmentType = new AssignmentType("AT 1", assignment.getAssignmentType().getGroup());
        assignmentTypeManager.save(assignmentType);
        assignment.setAssignmentType(assignmentType);

        assertCauses(Exception.class,
                () -> assignment.setAssignableDay(new AssignableDay(otherGroup, new LocalDate(2015, 1, 1))));
        assertCauses(Exception.class,
                () -> assignment.setAssignmentType(new AssignmentType("AT 2", otherGroup)));
        assertCauses(Exception.class,
                () -> new Assignment(assignment.getEmployee(),
                        assignment.getAssignableDay(),
                        new AssignmentType("AT 3", otherGroup)));
    }

    private Assignment createAssignment() {
        Group group = new Group("Core Services");
        Employee employee = new Employee("John Doe", "<yid>", ExtAppType.yammer);
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
