package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.fixtures.*;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yammer.stresstime.test.TestUtils.assertCauses;
import static com.yammer.stresstime.test.TestUtils.assertListOfEntitiesEqualsAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class AssignmentManagerTest extends BaseManagerTest<Assignment> {

    private AssignmentManager assignmentManager;
    private List<Assignment> testAssignments;
    private List<Group> groups;
    private List<Employee> employees;
    private List<AssignmentType> assignmentTypes;
    private List<Membership> memberships;
    private List<AssignableDay> assignableDays;
    private AssignmentsFixture assignmentsFixture;

    @Override
    protected EntityManager<Assignment> getEntityManager() {
        return assignmentManager;
    }

    @Override
    protected List<Assignment> getEntities() {
        return testAssignments;
    }

    @Override
    protected void initialize() {
        assignmentManager = new AssignmentManager(getSessionFactory());
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        groups = groupsFixture.getGroups();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        AssignmentTypesFixture assignmentTypesFixture = new AssignmentTypesFixture(groupsFixture);
        assignmentTypesFixture.save(getSessionFactory());
        assignmentTypes = assignmentTypesFixture.getAssignmentTypes();
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        membershipsFixture.save(getSessionFactory());
        memberships = membershipsFixture.getMemberships();
        AssignableDaysFixture assignableDaysFixture = new AssignableDaysFixture(groupsFixture);
        assignableDaysFixture.save(getSessionFactory());
        assignableDays = assignableDaysFixture.getAssignableDays();
        employees.stream().forEach(e -> refresh(e));
        groups.stream().forEach(g -> refresh(g));
        assignmentsFixture = new AssignmentsFixture(employeesFixture, assignmentTypesFixture, assignableDaysFixture);
        testAssignments = assignmentsFixture.getAssignments();
    }

    @Override
    protected void clean() {}

    void testEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<Assignment> found = assignmentManager.getByEmployeePeriod(employee, startDate, endDate);
        TestUtils.testAssignmentsEmployeePeriod(employee, startDate, endDate, testAssignments, found);
    }
    
    @Test
    public void testExists() {
        assignmentManager.save(testAssignments.get(0));
        assignmentManager.save(testAssignments.get(5));
        assertTrue(assignmentManager.exists(testAssignments.get(0)));
        assertTrue(assignmentManager.exists(testAssignments.get(5)));
        assertFalse(assignmentManager.exists(testAssignments.get(1)));
        assertFalse(assignmentManager.exists(testAssignments.get(10)));
    }

    @Test
    public void testGetByEmployeePeriod() {
        assignmentsFixture.save(getSessionFactory());
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

    @Test
    public void testGetStatistics(){
        assignmentsFixture.save(getSessionFactory());
        assignableDays.stream().forEach(a -> refresh(a));
        Map<Employee, Map<AssignmentType, Long>> statistics = assignmentManager.getStatistics(assignableDays);
        assignmentsFixture.checkStatistics(statistics);
    }
}
