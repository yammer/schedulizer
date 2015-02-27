package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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
        groups = Lists.newArrayList(new Group("group 1"), new Group("group 2"), new Group("group 3"));
        employees = Lists.newArrayList(new Employee("John", TestUtils.nextYammerId()),
                new Employee("Mary", TestUtils.nextYammerId()),
                new Employee("Louise", TestUtils.nextYammerId()));
        assignmentTypes = Lists.newArrayList(new AssignmentType("Primary", groups.get(0)),
                new AssignmentType("Secondary", groups.get(0)),
                new AssignmentType("Primary Support", groups.get(0)),
                new AssignmentType("AT 1", groups.get(1)),
                new AssignmentType("AT 2", groups.get(1)),
                new AssignmentType("AT 3", groups.get(1)),
                new AssignmentType("AT 4", groups.get(1)),
                new AssignmentType("Name", groups.get(2)));
        memberships = Lists.newArrayList(new Membership(employees.get(0), groups.get(1)),
                new Membership(employees.get(0), groups.get(0)),
                new Membership(employees.get(1), groups.get(1)),
                new Membership(employees.get(2), groups.get(0)),
                new Membership(employees.get(2), groups.get(2)));
        List<LocalDate> dates = Lists.newArrayList(new LocalDate(2015,2,10),
                new LocalDate(2015,2,11),
                new LocalDate(2015,2,12),
                new LocalDate(2015,2,13),
                new LocalDate(2015,2,14),
                new LocalDate(2015,2,15),
                new LocalDate(2015,2,16),
                new LocalDate(2015,2,17),
                new LocalDate(2015,2,18),
                new LocalDate(2015,2,19),
                new LocalDate(2015,2,20),
                new LocalDate(2015,3,7),
                new LocalDate(2015,4,12),
                new LocalDate(2015,12,17),
                new LocalDate(2016,2,29), // leap year ;)
                new LocalDate(2017,2,10));
        assignableDays = new ArrayList<>();
        dates.stream().forEach(d -> groups.stream().forEach(g -> assignableDays.add(new AssignableDay(g, d))));
        GroupManager groupManager = new GroupManager(getSessionFactory());
        AssignmentTypeManager assignmentTypeManager = new AssignmentTypeManager(getSessionFactory());
        EmployeeManager employeeManager = new EmployeeManager(getSessionFactory());
        MembershipManager membershipManager = new MembershipManager(getSessionFactory());
        AssignableDayManager assignableDayManager = new AssignableDayManager(getSessionFactory());
        groups.stream().forEach(g -> groupManager.save(g));
        assignmentTypes.stream().forEach(a -> assignmentTypeManager.save(a));
        employees.stream().forEach(e -> employeeManager.save(e));
        memberships.stream().forEach(m -> membershipManager.save(m));
        assignableDays.stream().forEach(a -> assignableDayManager.save(a));
        employees.stream().forEach(e -> refresh(e));
        groups.stream().forEach(g -> refresh(g));
        testAssignments = Lists.newArrayList(new Assignment(employees.get(0), assignableDays.get(0), assignmentTypes.get(0)),
                new Assignment(employees.get(0), assignableDays.get(3), assignmentTypes.get(0)),
                new Assignment(employees.get(0), assignableDays.get(6), assignmentTypes.get(0)),
                new Assignment(employees.get(0), assignableDays.get(9), assignmentTypes.get(1)),
                new Assignment(employees.get(0), assignableDays.get(0), assignmentTypes.get(1)),
                new Assignment(employees.get(0), assignableDays.get(9), assignmentTypes.get(2)),
                new Assignment(employees.get(0), assignableDays.get(12), assignmentTypes.get(2)),
                new Assignment(employees.get(0), assignableDays.get(15), assignmentTypes.get(2)),
                new Assignment(employees.get(1), assignableDays.get(18), assignmentTypes.get(0)),
                new Assignment(employees.get(1), assignableDays.get(21), assignmentTypes.get(0)),
                new Assignment(employees.get(1), assignableDays.get(18), assignmentTypes.get(1)),
                new Assignment(employees.get(1), assignableDays.get(24), assignmentTypes.get(1)),
                new Assignment(employees.get(1), assignableDays.get(27), assignmentTypes.get(2)),
                new Assignment(employees.get(1), assignableDays.get(30), assignmentTypes.get(2)),
                new Assignment(employees.get(1), assignableDays.get(33), assignmentTypes.get(2)),
                new Assignment(employees.get(2), assignableDays.get(1), assignmentTypes.get(3)),
                new Assignment(employees.get(2), assignableDays.get(4), assignmentTypes.get(3)),
                new Assignment(employees.get(2), assignableDays.get(7), assignmentTypes.get(3)),
                new Assignment(employees.get(2), assignableDays.get(22), assignmentTypes.get(4)),
                new Assignment(employees.get(2), assignableDays.get(16), assignmentTypes.get(4)),
                new Assignment(employees.get(2), assignableDays.get(31), assignmentTypes.get(5)),
                new Assignment(employees.get(2), assignableDays.get(13), assignmentTypes.get(6)),
                new Assignment(employees.get(2), assignableDays.get(10), assignmentTypes.get(6)),
                new Assignment(employees.get(2), assignableDays.get(19), assignmentTypes.get(6)),
                new Assignment(employees.get(1), assignableDays.get(1), assignmentTypes.get(3)),
                new Assignment(employees.get(1), assignableDays.get(22), assignmentTypes.get(3)),
                new Assignment(employees.get(1), assignableDays.get(31), assignmentTypes.get(5)),
                new Assignment(employees.get(1), assignableDays.get(13), assignmentTypes.get(5)),
                new Assignment(employees.get(1), assignableDays.get(10), assignmentTypes.get(5)),
                new Assignment(employees.get(1), assignableDays.get(19), assignmentTypes.get(6)),
                new Assignment(employees.get(0), assignableDays.get(2), assignmentTypes.get(7)),
                new Assignment(employees.get(1), assignableDays.get(5), assignmentTypes.get(7)),
                new Assignment(employees.get(1), assignableDays.get(8), assignmentTypes.get(7)),
                new Assignment(employees.get(1), assignableDays.get(23), assignmentTypes.get(7)),
                new Assignment(employees.get(2), assignableDays.get(17), assignmentTypes.get(7)),
                new Assignment(employees.get(2), assignableDays.get(32), assignmentTypes.get(7)),
                new Assignment(employees.get(2), assignableDays.get(14), assignmentTypes.get(7)),
                new Assignment(employees.get(2), assignableDays.get(11), assignmentTypes.get(7)));
    }

    @Override
    protected void clean() {}

    void testEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<Assignment> expected = testAssignments
            .stream()
            .filter(a -> (a.getEmployee().equals(employee) &&
                    ((a.getAssignableDay().getDate().isAfter(startDate) && a.getAssignableDay().getDate().isBefore(endDate)) ||
                            a.getAssignableDay().getDate().isEqual(startDate) ||
                            a.getAssignableDay().getDate().isEqual(endDate))))
            .collect(Collectors.toList());
        if (startDate.isAfter(endDate)) {
            expected = Lists.newArrayList();
        }
        List<Assignment> found = assignmentManager.getByEmployeePeriod(employee, startDate, endDate);
        assertListOfEntitiesEqualsAnyOrder(expected, found);
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
        testAssignments.stream().forEach(a -> assignmentManager.save(a));
        testEmployeePeriod(employees.get(0), new LocalDate(2015,1,10), new LocalDate(2015,1,12));
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
        testEmployeePeriod(employees.get(2), new LocalDate(2014,12,31), new LocalDate(2015,1,12));
    }
}
