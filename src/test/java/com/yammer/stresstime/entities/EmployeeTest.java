package com.yammer.stresstime.entities;

import com.yammer.stresstime.managers.EmployeeManager;
import com.yammer.stresstime.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertNotNull;

public class EmployeeTest extends DatabaseTest {

    private EmployeeManager mEmployeeManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mEmployeeManager = new EmployeeManager(getSessionFactory());
    }

    @Test
    public void testNewHasEmptyAssignmentsAfterRetrievedFromDb() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setYammerId("lorem");
        mEmployeeManager.save(employee);
        refresh(employee);

        assertNotNull(employee.getAssignments());
        assertThat(employee.getAssignments(), empty());

        mEmployeeManager.delete(employee);
    }
}
