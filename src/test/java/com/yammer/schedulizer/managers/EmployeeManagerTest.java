package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.fixtures.EmployeesFixture;
import com.yammer.schedulizer.test.TestUtils;
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
        EmployeesFixture employeesFixture = new EmployeesFixture();
        testEmployees = employeesFixture.getEmployees();
    }

    @Override
    protected void clean() {}

    @Test
    public void testFindByExtAppIdRetrievesTheCorrectRecord() {
        Employee employee = testEmployees.get(0);
        employeeManager.save(employee);
        refresh(employee);
        Employee found = employeeManager.getByExtAppId(employee.getExtAppId(), ExtAppType.yammer);

        assertNotNull(found);
        assertThat(found, equalTo(employee));
        employeeManager.delete(employee);
    }

    @Test
    public void testGetGlobalAdminsRetrievesTheCorrectRecord() {
        String gloalAdminExtAppId = TestUtils.nextExtAppId();
        Employee globalAdmin = new Employee("John Doe", gloalAdminExtAppId, ExtAppType.yammer);
        globalAdmin.setGlobalAdmin(true);
        employeeManager.save(globalAdmin);
        refresh(globalAdmin);
        String extAppId = TestUtils.nextExtAppId();
        Employee employee = new Employee("Mary", extAppId, ExtAppType.yammer);
        employeeManager.save(employee);
        List<Employee> globalAdmins = employeeManager.getGlobalAdmins();
        assertNotNull(globalAdmins);
        assertThat(globalAdmins.size(), equalTo(1));
        Employee found = globalAdmins.get(0);
        assertThat(found, equalTo(globalAdmin));
        employeeManager.delete(globalAdmin);
        employeeManager.delete(employee);
    }

    @Test
    public void testGetOrCreateByExtAppId() {
        String extAppId = TestUtils.nextExtAppId();
        Employee employee = new Employee("John Doe", extAppId, ExtAppType.yammer);
        employeeManager.save(employee);
        Employee found = employeeManager.getOrCreateByExtAppId(employee.getExtAppId(), ExtAppType.yammer, (Employee e) -> {
        });
        assertNotNull(found);
        assertThat(found, equalTo(employee));
        Employee newEmployee = employeeManager.getOrCreateByExtAppId(TestUtils.nextExtAppId(), ExtAppType.yammer,(Employee e) -> {
            e.setName("Amanda");
            e.setImageUrlTemplate("lol");
        });
        Employee newFound = employeeManager.getByExtAppId(newEmployee.getExtAppId(), ExtAppType.yammer);
        assertNotNull(newFound);
        assertThat(newFound, equalTo(newEmployee));
    }
}
