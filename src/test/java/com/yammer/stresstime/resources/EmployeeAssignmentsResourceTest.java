package com.yammer.stresstime.resources;

import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.fixtures.*;
import com.yammer.stresstime.managers.AssignmentManager;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class EmployeeAssignmentsResourceTest extends BaseResourceTest {

    private List<Assignment> assignments;
    private List<Employee> employees;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        AssignmentTypesFixture assignmentTypesFixture = new AssignmentTypesFixture(groupsFixture);
        assignmentTypesFixture.save(getSessionFactory());
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        membershipsFixture.save(getSessionFactory());
        AssignableDaysFixture assignableDaysFixture = new AssignableDaysFixture(groupsFixture);
        assignableDaysFixture.save(getSessionFactory());
        employees.stream().forEach(e -> refresh(e));
        groupsFixture.getGroups().stream().forEach(g -> refresh(g));
        AssignmentsFixture assignmentsFixture =
                new AssignmentsFixture(employeesFixture, assignmentTypesFixture, assignableDaysFixture);
        assignmentsFixture.save(getSessionFactory());
        assignments = assignmentsFixture.getAssignments();
    }

    private void testEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        UriBuilder uriBuilder = UriBuilder.fromPath("/employees")
                .path("/{employee_id}/assignments")
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString());
        // Only the employee can see its own assignments using this method
        setCurrentUser(employee);
        String path = uriBuilder.build(employee.getId()).toString();
        List<Assignment> found = Arrays.asList(resource(path).get(Assignment[].class));
        List<Map<String, String>> response = Arrays.asList(resource(path).get(Map[].class));
        // Tested manually because restAssignmentsEmployeePeriod only checks for id
        response.stream().forEach(r -> {
            assertNotNull(r.get("assignmentTypeId"));
            assertNotNull(r.get("employeeId"));
            assertNotNull(r.get("date"));
            assertNotNull(r.get("assignmentTypeName"));
            assertNotNull(r.get("group"));
        });
        TestUtils.testAssignmentsEmployeePeriod(employee, startDate, endDate, assignments, found);
    }

    @Test
    public void testGetAssignments() {
        testEmployeePeriod(employees.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,2,10), new LocalDate(2015,2,20));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,2,15), new LocalDate(2015,1,12));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,2,5), new LocalDate(2015,2,28));
        testEmployeePeriod(employees.get(0), new LocalDate(2016,2,10), new LocalDate(2016,2,29));
        testEmployeePeriod(employees.get(0), new LocalDate(2016,1,10), new LocalDate(2016,3,1));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,12,10), new LocalDate(2015,12,12));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,1,10), new LocalDate(2015,3,31));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,1,10), new LocalDate(2015,2,12));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,5,17), new LocalDate(2015,6,20));
        testEmployeePeriod(employees.get(0), new LocalDate(2014,12,31), new LocalDate(2015,1,12));

        testEmployeePeriod(employees.get(1), new LocalDate(2015,1,10), new LocalDate(2015,1,12));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,2,10), new LocalDate(2015,2,20));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,2,15), new LocalDate(2015,1,12));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,2,5), new LocalDate(2015,2,28));
        testEmployeePeriod(employees.get(1), new LocalDate(2016,2,10), new LocalDate(2016,2,29));
        testEmployeePeriod(employees.get(1), new LocalDate(2016,1,10), new LocalDate(2016,3,1));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,12,10), new LocalDate(2015,12,12));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,1,10), new LocalDate(2015,3,31));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,1,10), new LocalDate(2015,2,12));
        testEmployeePeriod(employees.get(1), new LocalDate(2015,5,17), new LocalDate(2015,6,20));
        testEmployeePeriod(employees.get(1), new LocalDate(2014,12,31), new LocalDate(2015,1,12));

        testEmployeePeriod(employees.get(2), new LocalDate(2015,1,10), new LocalDate(2015,1,12));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,2,10), new LocalDate(2015,2,20));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,2,15), new LocalDate(2015,1,12));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,2,5), new LocalDate(2015,2,28));
        testEmployeePeriod(employees.get(2), new LocalDate(2016,2,10), new LocalDate(2016,2,29));
        testEmployeePeriod(employees.get(2), new LocalDate(2016,1,10), new LocalDate(2016,3,1));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,12,10), new LocalDate(2015,12,12));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,1,10), new LocalDate(2015,3,31));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,1,10), new LocalDate(2015,2,12));
        testEmployeePeriod(employees.get(2), new LocalDate(2015,5,17), new LocalDate(2015,6,20));
        testEmployeePeriod(employees.get(2), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));
    }
}
