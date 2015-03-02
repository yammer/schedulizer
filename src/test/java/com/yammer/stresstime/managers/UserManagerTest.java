package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.User;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.yammer.stresstime.test.TestUtils.assertCauses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserManagerTest extends BaseManagerTest<User> {

    private UserManager userManager;
    private List<User> testUsers;
    private List<Employee> employees;

    @Override
    protected EntityManager<User> getEntityManager() {
        return userManager;
    }

    @Override
    protected List<User> getEntities() {
        return testUsers;
    }

    @Override
    protected void initialize() {
        userManager = new UserManager(getSessionFactory());
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        testUsers = employees.stream().map(e -> new User(e, null)).collect(Collectors.toList());
    }

    @Override
    protected void clean() {}

    @Test
    public void testFindByYammerIdRetrievesTheCorrectRecord() {
        User user = testUsers.get(0);
        userManager.save(user);
        User found = userManager.safeGetByYammerId(user.getEmployee().getYammerId());
        assertNotNull(found);
        assertThat(found, equalTo(user));
        userManager.delete(user);
    }

    @Test
    public void testTrySaveGuest() {
        assertCauses(IllegalStateException.class, () -> { userManager.save(User.guest()); });
    }
}
