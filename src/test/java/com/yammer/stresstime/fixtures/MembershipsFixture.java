package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.managers.MembershipManager;
import org.hibernate.SessionFactory;

import java.util.List;

public class MembershipsFixture {

    private List<Membership> memberships;
    private EmployeesFixture employeesFixture;
    private GroupsFixture groupsFixture;
    private boolean saved;

    public MembershipsFixture(EmployeesFixture employeesFixture, GroupsFixture groupsFixture) {
        this.groupsFixture = groupsFixture;
        this.employeesFixture = employeesFixture;
        saved = false;
        List<Employee> employees = employeesFixture.getEmployees();
        List<Group> groups = groupsFixture.getGroups();
        memberships = Lists.newArrayList(new Membership(employees.get(0), groups.get(0)),
                new Membership(employees.get(0), groups.get(1)),
                new Membership(employees.get(1), groups.get(1)),
                new Membership(employees.get(1), groups.get(2)),
                new Membership(employees.get(2), groups.get(0)),
                new Membership(employees.get(2), groups.get(1)),
                new Membership(employees.get(2), groups.get(2)));
    }

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        groupsFixture.save(sessionFactory);
        employeesFixture.save(sessionFactory);
        MembershipManager membershipManager = new MembershipManager(sessionFactory);
        memberships.stream().forEach(e -> membershipManager.save(e));
    }

    public List<Membership> getMemberships() {
        return memberships;
    }
}
