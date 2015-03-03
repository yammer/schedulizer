package com.yammer.stresstime.resources;

import com.google.common.collect.Lists;
import com.yammer.stresstime.auth.Role;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.managers.GroupManager;
import com.yammer.stresstime.managers.MembershipManager;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AuthorizationResourceTest extends BaseResourceTest{

    private List<Employee> employees;

    public void setUp() throws Exception {
        super.setUp();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
    }

    @Test
    public void testGlobalAdmin() {
        setCurrentUser(getGlobalAdmin());
        Map<String,Object> global = resource("/current").get(Map.class);
        assertThat(global.get("role"), equalTo(Role.ADMIN.toString()));
        assertThat(new Long((Integer)global.get("employeeId")), equalTo(getGlobalAdmin().getId()));
        assertThat(global.get("groupsAdmin"), equalTo(Lists.newArrayList()));
    }

    @Test
    public void testMembers() {
        employees.stream().forEach(e -> {
            setCurrentUser(e);
            Map<String,Object> current = resource("/current").get(Map.class);
            assertThat(current.get("role"), equalTo(Role.MEMBER.toString()));
            assertThat(new Long((Integer)current.get("employeeId")), equalTo(e.getId()));
            assertThat(current.get("groupsAdmin"), equalTo(Lists.newArrayList()));
        });
    }

    @Test
    public void testGuest() {
        setCurrentUser(null);
        Map<String,Object> current = resource("/current").get(Map.class);
        assertThat(current.get("role"), equalTo(Role.GUEST.toString()));
        assertThat(current.get("groupsAdmin"), equalTo(null));
        assertThat(current.get("employeeId"), equalTo(null));
    }

    public void testGroupAdmins() {
        GroupManager groupManager = new GroupManager(getSessionFactory());
        MembershipManager membershipManager = new MembershipManager(getSessionFactory());
        Group group = new Group("Core Services");
        groupManager.save(group);
        Membership membership = new Membership(employees.get(0), group);
        membership.setAdmin(true);
        membershipManager.save(membership);

        setCurrentUser(employees.get(0));
        Map<String,Object> current = resource("/current").get(Map.class);
        assertThat(current.get("role"), equalTo(Role.MEMBER.toString()));
        assertThat(current.get("groupsAdmin"), equalTo(Lists.newArrayList(group.getId())));
    }
}
