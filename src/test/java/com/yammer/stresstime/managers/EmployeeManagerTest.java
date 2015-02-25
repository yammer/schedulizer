package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

public class EmployeeManagerTest extends DatabaseTest {

    private EmployeeManager employeeManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        employeeManager = new EmployeeManager(getSessionFactory());
    }

    @Test
    public void testFindByYammerIdRetrievesTheCorrectRecord() {
        String yammerId = TestUtils.nextYammerId();
        Employee employee = new Employee("John Doe", yammerId);
        employeeManager.save(employee);
        refresh(employee);
        Employee found = employeeManager.getByYammerId(yammerId);

        assertNotNull(found);
        assertThat(found, equalTo(employee));

        employeeManager.delete(employee);
    }

    @Test
    public void testGetGlobalAdminsRetrievesTheCorrectRecord() {
        String gloalAdminYammerId = TestUtils.nextYammerId();
        Employee globalAdmin = new Employee("John Doe", gloalAdminYammerId);
        globalAdmin.setGlobalAdmin(true);
        employeeManager.save(globalAdmin);
        refresh(globalAdmin);
        String yammerId = TestUtils.nextYammerId();
        Employee employee = new Employee("Mary", yammerId);
        employeeManager.save(employee);
        List<Employee> globalAdmins = employeeManager.getGlobalAdmins();
        assertNotNull(globalAdmins);
        assertThat(globalAdmins.size(), equalTo(1));
        Employee found = globalAdmins.get(0);
        assertThat(found, equalTo(globalAdmin));
        employeeManager.delete(globalAdmin);
        employeeManager.delete(employee);
    }
}
