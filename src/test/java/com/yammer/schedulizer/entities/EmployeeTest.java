package com.yammer.schedulizer.entities;

import com.yammer.schedulizer.auth.ExtAppType;
import com.yammer.schedulizer.managers.EmployeeManager;
import com.yammer.schedulizer.test.DatabaseTest;
import com.yammer.schedulizer.test.TestUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.schedulizer.test.TestUtils.assertCauses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertNotNull;

public class EmployeeTest extends DatabaseTest {

    private EmployeeManager employeeManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        employeeManager = new EmployeeManager(getSessionFactory());
    }

    @Test
    public void testNewHasEmptyAssignmentsAfterRetrievedFromDb() {
        Employee employee = new Employee("John Doe", TestUtils.nextExtAppId(), ExtAppType.yammer);
        employeeManager.save(employee);
        refresh(employee);

        assertNotNull(employee.getAssignments());
        assertThat(employee.getAssignments(), empty());

        employeeManager.delete(employee);
    }

    @Test
    public void testEmployeeUniqueness() throws Exception {
        String yammerId = TestUtils.nextExtAppId();
        Employee employee = new Employee("John Doe", yammerId, ExtAppType.yammer);
        employeeManager.save(employee);

        Employee clone = new Employee("Barak Obama", yammerId, ExtAppType.yammer);

        assertCauses(ConstraintViolationException.class, () -> employeeManager.save(clone));

        hibernateThrewException();
    }
}
