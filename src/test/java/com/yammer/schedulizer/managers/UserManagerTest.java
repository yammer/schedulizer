package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.auth.ExtAppAuthenticatorFactory;
import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.entities.User;
import com.yammer.schedulizer.fixtures.EmployeesFixture;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.yammer.schedulizer.test.TestUtils.assertCauses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

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
        User found = userManager.safeGetByExtAppId(user.getEmployee().getExtAppId());
        assertNotNull(found);
        assertThat(found, equalTo(user));
        userManager.delete(user);
    }

    @Test
    public void testTrySaveGuest() {
        assertCauses(IllegalStateException.class, () -> { userManager.save(User.guest()); });
    }
}
