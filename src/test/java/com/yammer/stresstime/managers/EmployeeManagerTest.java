package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

public class EmployeeManagerTest extends DatabaseTest {

    private EmployeeManager mEmployeeManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mEmployeeManager = new EmployeeManager(getSessionFactory());
    }

    @Test
    public void testFindByYammerIdRetrievesTheCorrectRecord() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setYammerId("lorem");
        mEmployeeManager.save(employee);
        refresh(employee);
        Employee found = mEmployeeManager.getByYammerId("lorem");

        assertNotNull(found);
        assertThat(found, equalTo(employee));

        mEmployeeManager.delete(employee);
    }
}
