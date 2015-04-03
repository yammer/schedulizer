package com.yammer.schedulizer.entities;

import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.managers.GroupManager;
import com.yammer.schedulizer.managers.MembershipManager;
import com.yammer.schedulizer.test.DatabaseTest;
import com.yammer.schedulizer.test.TestUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.schedulizer.test.TestUtils.assertCauses;

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
    public void testMembershipUniqueness() throws Exception {
        Employee employee = new Employee("John Doe", TestUtils.nextExtAppId());
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
