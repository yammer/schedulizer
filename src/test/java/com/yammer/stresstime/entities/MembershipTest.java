package com.yammer.stresstime.entities;

import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.stresstime.test.TestUtils.assertCauses;

public class MembershipTest extends DatabaseTest {

    private GroupManager groupManager;
    private EmployeeManager employeeManager;
    private MembershipManager membershipManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        groupManager = new GroupManager(getSessionFactory());
        employeeManager = new EmployeeManager(getSessionFactory());
        membershipManager = new MembershipManager(getSessionFactory());
    }

    @Test
    public void testAssignmentUniqueness() throws Exception {
        Employee employee = new Employee("John Doe", "<yid>");
        Group group = new Group("Core Services");
        employeeManager.save(employee);
        groupManager.save(group);
        membershipManager.join(group, employee);

        assertCauses(ConstraintViolationException.class, () -> membershipManager.join(group, employee));

        Membership membership = new Membership(employee, group);
        assertCauses(ConstraintViolationException.class, () -> membershipManager.save(membership));

        hibernateThrewException();
    }
}
