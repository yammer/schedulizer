package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.yammer.stresstime.test.TestUtils.assertListOfEntitiesEqualsAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class AssignableDayManagerTest extends BaseManagerTest<AssignableDay> {

    private AssignableDayManager assignableDayManager;
    private List<Group> groups;
    private List<Employee> employees;
    private List<AssignmentType> assignmentTypes;
    private List<Membership> memberships;
    private List<AssignableDay> testAssignableDays;

    @Override
    protected EntityManager<AssignableDay> getEntityManager() {
        return assignableDayManager;
    }

    @Override
    protected List<AssignableDay> getEntities() {
        return testAssignableDays;
    }

    @Override
    protected void initialize() {
        assignableDayManager = new AssignableDayManager(getSessionFactory());
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
        GroupManager groupManager = new GroupManager(getSessionFactory());
        AssignmentTypeManager assignmentTypeManager = new AssignmentTypeManager(getSessionFactory());
        EmployeeManager employeeManager = new EmployeeManager(getSessionFactory());
        MembershipManager membershipManager = new MembershipManager(getSessionFactory());
        groups.stream().forEach(g -> groupManager.save(g));
        assignmentTypes.stream().forEach(a -> assignmentTypeManager.save(a));
        employees.stream().forEach(e -> employeeManager.save(e));
        memberships.stream().forEach(m -> membershipManager.save(m));
        employees.stream().forEach(e -> refresh(e));
        groups.stream().forEach(g -> refresh(g));

        testAssignableDays = new ArrayList<>();
        dates.stream().forEach(d -> groups.stream().forEach(g -> testAssignableDays.add(new AssignableDay(g, d))));
    }

    @Override
    protected void clean() {}

    void testPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        List<AssignableDay> expected = testAssignableDays
                .stream()
                .filter(a -> (a.getGroup().equals(group) &&
                        ((a.getDate().isAfter(startDate) && a.getDate().isBefore(endDate)) ||
                                a.getDate().isEqual(startDate) ||
                                a.getDate().isEqual(endDate))))
                .collect(Collectors.toList());
        if (startDate.isAfter(endDate)) {
            expected = Lists.newArrayList();
        }
        List<AssignableDay> found = assignableDayManager.getByGroupPeriod(group, startDate, endDate);
        assertListOfEntitiesEqualsAnyOrder(expected, found);
    }


    @Test
    public void testGetByGroupPeriod() {
        testAssignableDays.stream().forEach(a -> assignableDayManager.save(a));
        testPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(0), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testPeriod(groups.get(0), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(0), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testPeriod(groups.get(0), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testPeriod(groups.get(0), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testPeriod(groups.get(0), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 3, 31));
        testPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testPeriod(groups.get(0), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testPeriod(groups.get(0), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));

        testPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(1), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testPeriod(groups.get(1), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(1), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testPeriod(groups.get(1), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testPeriod(groups.get(1), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testPeriod(groups.get(1), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 3, 31));
        testPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testPeriod(groups.get(1), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testPeriod(groups.get(1), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));

        testPeriod(groups.get(2), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(2), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testPeriod(groups.get(2), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testPeriod(groups.get(2), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testPeriod(groups.get(2), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testPeriod(groups.get(2), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testPeriod(groups.get(2), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testPeriod(groups.get(2), new LocalDate(2015,1,10), new LocalDate(2015,3,31));
        testPeriod(groups.get(2), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testPeriod(groups.get(2), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testPeriod(groups.get(2), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));
    }

    @Test
    public void testGetOrCreateByGroupAndDate() {
        AssignableDay assignableDay0 = assignableDayManager.getOrCreateByGroupAndDate(
                testAssignableDays.get(0).getGroup(),
                testAssignableDays.get(0).getDate());
        AssignableDay assignableDay1 = assignableDayManager.getOrCreateByGroupAndDate(
                testAssignableDays.get(1).getGroup(),
                testAssignableDays.get(1).getDate());
        AssignableDay found0 = assignableDayManager.getById(assignableDay0.getId());
        AssignableDay found1 = assignableDayManager.getById(assignableDay1.getId());
        assertNotNull(found0);
        assertNotNull(found1);
        assertThat(found0, equalTo(assignableDay0));
        assertThat(found1, equalTo(assignableDay1));
        AssignableDay found0Again = assignableDayManager.getOrCreateByGroupAndDate(
                assignableDay0.getGroup(),
                assignableDay0.getDate());
        assertNotNull(found0Again);
        assertThat(found0Again, equalTo(assignableDay0));
    }
}
