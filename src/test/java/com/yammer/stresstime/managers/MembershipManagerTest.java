package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.fixtures.GroupsFixture;
import com.yammer.stresstime.fixtures.MembershipsFixture;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

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
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        groups = groupsFixture.getGroups();
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        testMemberships = membershipsFixture.getMemberships();
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
