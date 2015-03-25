package com.yammer.schedulizer.resources;

import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.Group;
import com.yammer.schedulizer.entities.Membership;
import com.yammer.schedulizer.fixtures.EmployeesFixture;
import com.yammer.schedulizer.fixtures.GroupsFixture;
import com.yammer.schedulizer.fixtures.MembershipsFixture;
import com.yammer.schedulizer.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class EmployeesResourceTest extends BaseResourceTest {
    private List<Employee> employees;
    private List<Membership> memberships;

    @Before
    public void setUp() throws Exception{
        super.setUp();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        membershipsFixture.save(getSessionFactory());
        memberships = membershipsFixture.getMemberships();
    }

    @Test
    public void testGetEmployee() {
        employees.stream().forEach(e -> {
            Employee found = resource(String.format("/employees/%s", e.getId())).get(Employee.class);
            assertNotNull(found);
            // Testing each parameter because method equals only tests id
            assertThat(found.getName(), equalTo(e.getName()));
            assertThat(found.getId(), equalTo(e.getId()));
            assertThat(found.getImageUrlTemplate(), equalTo(e.getImageUrlTemplate()));
        });
    }

    @Test
    public void testGetEmployeeGroups() {
        employees.stream().forEach(e -> {
            List<Group> found =
                    Arrays.asList(resource(String.format("/employees/%s/groups", e.getId())).get(Group[].class));
            assertNotNull(found);
            List<Group> expected = memberships.stream()
                    .filter(m -> m.getEmployee().equals(e))
                    .map(Membership::getGroup)
                    .collect(Collectors.toList());
            TestUtils.assertListOfEntitiesEqualsAnyOrder(found, expected);
        });
    }
}
