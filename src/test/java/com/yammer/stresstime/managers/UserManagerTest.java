package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
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

public class UserManagerTest extends BaseManagerTest<User> {

    private UserManager userManager;
    List<User> testUsers;

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
        EmployeeManager employeeManager = new EmployeeManager(getSessionFactory());
        Employee john = new Employee("John", TestUtils.nextYammerId());
        Employee mary = new Employee("Mary", TestUtils.nextYammerId());
        Employee catlin = new Employee("Catlin", TestUtils.nextYammerId());
        employeeManager.save(john);
        employeeManager.save(mary);
        employeeManager.save(catlin);
        testUsers = Lists.newArrayList(new User(john, null),
                new User(mary, null),
                new User(catlin, null));
    }

    @Test
    public void testFindByYammerIdRetrievesTheCorrectRecord() {
        User user = testUsers.get(0);
        userManager.save(user);
        refresh(user);
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
