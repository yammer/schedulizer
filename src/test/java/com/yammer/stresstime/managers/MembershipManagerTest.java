package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
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

public class MembershipManagerTest extends BaseManagerTest<Membership> {

    private MembershipManager membershipManager;
    private List<Membership> testMemberships;
    private List<Group> groups;
    private List<Employee> employees;

    @Override
    protected EntityManager<Membership> getEntityManager() {
        return membershipManager;
    }

    @Override
    protected List<Membership> getEntities() {
        return testMemberships;
    }

    @Override
    protected void initialize() {
        membershipManager = new MembershipManager(getSessionFactory());
        employees = Lists.newArrayList(new Employee("John", TestUtils.nextYammerId()),
                new Employee("Mary", TestUtils.nextYammerId()),
                new Employee("Louise", TestUtils.nextYammerId()));
        groups = Lists.newArrayList(new Group("Core Services"), new Group("API"), new Group("IOS"));
        EmployeeManager employeeManager = new EmployeeManager(getSessionFactory());
        GroupManager groupManager = new GroupManager(getSessionFactory());
        employees.stream().forEach(e -> employeeManager.save(e));
        groups.stream().forEach(g -> groupManager.save(g));
        testMemberships = Lists.newArrayList(new Membership(employees.get(0), groups.get(0)),
                new Membership(employees.get(0), groups.get(1)),
                new Membership(employees.get(1), groups.get(1)),
                new Membership(employees.get(1), groups.get(2)),
                new Membership(employees.get(2), groups.get(0)),
                new Membership(employees.get(2), groups.get(1)),
                new Membership(employees.get(2), groups.get(2)));
    }

    @Override
    protected void clean() {}

    @Test
    public void testJoin() {
        Membership membership = membershipManager.join(groups.get(2), employees.get(0));
        Membership found = membershipManager.getById(membership.getId());
        assertNotNull(found);
        assertThat(found, equalTo(membership));
    }

    @Test
    public void testGetByEmployeeIdAndGroupId() {
        Membership membership = membershipManager.join(groups.get(1), employees.get(2));
        Membership found = membershipManager.getByEmployeeIdAndGroupId(employees.get(2).getId(), groups.get(1).getId());
        assertNotNull(found);
        assertThat(found, equalTo(membership));
    }
}
