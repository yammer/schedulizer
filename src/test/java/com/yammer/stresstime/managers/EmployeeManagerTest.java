package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

public class EmployeeManagerTest extends BaseManagerTest<Employee> {

    private EmployeeManager employeeManager;
    List<Employee> testEmployees;

    @Override
    protected EntityManager<Employee> getEntityManager() {
        return employeeManager;
    }

    @Override
    protected List<Employee> getEntities() {
        return testEmployees;
    }

    @Override
    protected void initialize() {
        employeeManager = new EmployeeManager(getSessionFactory());
        testEmployees = Lists.newArrayList(new Employee("John", TestUtils.nextYammerId()),
                new Employee("Mary", TestUtils.nextYammerId()),
                new Employee("Louise", TestUtils.nextYammerId()));
    }

    @Test
    public void testFindByYammerIdRetrievesTheCorrectRecord() {
        Employee employee = testEmployees.get(0);
        employeeManager.save(employee);
        refresh(employee);
        Employee found = employeeManager.getByYammerId(employee.getYammerId());

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
