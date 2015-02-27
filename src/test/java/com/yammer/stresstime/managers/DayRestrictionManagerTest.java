package com.yammer.stresstime.managers;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.test.DatabaseTest;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.cglib.core.Local;

import java.util.List;
import java.util.stream.Collectors;

import static com.yammer.stresstime.test.TestUtils.assertCauses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DayRestrictionManagerTest extends BaseManagerTest<DayRestriction> {

    private DayRestrictionManager dayRestrictionManager;
    private List<DayRestriction> testDayRestrictions;
    private List<Employee> employees;

    @Override
    protected EntityManager<DayRestriction> getEntityManager() {
        return dayRestrictionManager;
    }

    @Override
    protected List<DayRestriction> getEntities() {
        return testDayRestrictions;
    }

    @Override
    protected void initialize() {
        dayRestrictionManager = new DayRestrictionManager(getSessionFactory());
        employees = Lists.newArrayList(new Employee("John", TestUtils.nextYammerId()),
                new Employee("Mary", TestUtils.nextYammerId()),
                new Employee("Louise", TestUtils.nextYammerId()));
        employees.stream().forEach(e -> currentSession().save(e));
        testDayRestrictions = Lists.newArrayList(new DayRestriction(new LocalDate(2015,2,9), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,11), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,15), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,12), employees.get(0)),
                new DayRestriction(new LocalDate(2015,3,15), employees.get(0)),
                new DayRestriction(new LocalDate(2015,5,10), employees.get(0)),
                new DayRestriction(new LocalDate(2015,2,11), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,12), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,13), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,15), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,20), employees.get(1)),
                new DayRestriction(new LocalDate(2015,2,10), employees.get(2)));
    }

    @Override
    protected void clean() {}

    void testPeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<DayRestriction> expected = testDayRestrictions
                .stream()
                .filter(d -> (d.getEmployee().equals(employee) &&
                        ((d.getDate().isAfter(startDate) && d.getDate().isBefore(endDate)) ||
                                d.getDate().isEqual(startDate) ||
                                d.getDate().isEqual(endDate))))
                .collect(Collectors.toList());
        if (startDate.isAfter(endDate)) {
            expected = Lists.newArrayList();
        }
        List<DayRestriction> found = dayRestrictionManager.getByEmployeePeriod(employee, startDate, endDate);
        assertArrayEquals(expected.toArray(), found.toArray());
    }

    void testGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        List<DayRestriction> expected = testDayRestrictions
                .stream()
                .filter(d -> (group.getEmployees().contains(d.getEmployee()) &&
                        ((d.getDate().isAfter(startDate) && d.getDate().isBefore(endDate)) ||
                                d.getDate().isEqual(startDate) ||
                                d.getDate().isEqual(endDate))))
                .collect(Collectors.toList());
        if (startDate.isAfter(endDate)) {
            expected = Lists.newArrayList();
        }
        List<DayRestriction> found = dayRestrictionManager.getByGroupPeriod(group, startDate, endDate);
        assertArrayEquals(expected.toArray(), found.toArray());
    }

    @Test
    public void testGetByEmployeePeriod() {
        testDayRestrictions.stream().forEach(d -> dayRestrictionManager.save(d));
        testPeriod(employees.get(0), new LocalDate(2015,2,10), new LocalDate(2015,3,10));
        testPeriod(employees.get(0), new LocalDate(2015,2,5), new LocalDate(2015,2,10));
        testPeriod(employees.get(0), new LocalDate(2020,2,5), new LocalDate(2015,2,10));
        testPeriod(employees.get(0), new LocalDate(2020,2,5), new LocalDate(2050,2,10));
        testPeriod(employees.get(1), new LocalDate(2015,2,10), new LocalDate(2016,3,10));
        testPeriod(employees.get(1), new LocalDate(2015,2,11), new LocalDate(2015,2,11));
        testPeriod(employees.get(2), new LocalDate(2020,2,10), new LocalDate(2015,2,10));
        testPeriod(employees.get(2), new LocalDate(2020,2,11), new LocalDate(2050,2,22));
    }

    @Test
    public void testGetOrCreateByEmployeeAndDate() {
        DayRestriction dayRestriction =
                dayRestrictionManager.getOrCreateByEmployeeAndDate(employees.get(0), new LocalDate(2015, 10, 10));
        DayRestriction found = dayRestrictionManager.getById(dayRestriction.getId());
        assertNotNull(found);
        assertThat(found, equalTo(dayRestriction));
        dayRestrictionManager.save(testDayRestrictions.get(0));
        found = dayRestrictionManager.getOrCreateByEmployeeAndDate(testDayRestrictions.get(0).getEmployee(),
                                                           testDayRestrictions.get(0).getDate());
        assertNotNull(found);
        assertThat(found, equalTo(testDayRestrictions.get(0)));
    }

    @Test
    public void testGetByGroupPeriod() {
        testDayRestrictions.stream().forEach(d -> dayRestrictionManager.save(d));
        GroupManager groupManager = new GroupManager(getSessionFactory());
        Group group1 = new Group("group 1");
        Group group2 = new Group("group 2");
        Group group3 = new Group("group 3");
        Group group4 = new Group("group 4");
        groupManager.save(group1);
        groupManager.save(group2);
        groupManager.save(group3);
        groupManager.save(group4);
        MembershipManager membershipManager = new MembershipManager(getSessionFactory());
        membershipManager.join(group1, employees.get(0));
        membershipManager.join(group1, employees.get(2));
        membershipManager.join(group2, employees.get(0));
        membershipManager.join(group2, employees.get(1));
        membershipManager.join(group3, employees.get(1));
        refresh(group1);
        refresh(group2);
        refresh(group3);
        refresh(group4);
        testGroupPeriod(group1, new LocalDate(2015, 2, 10), new LocalDate(2015, 3, 10));
        testGroupPeriod(group1, new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 10));
        testGroupPeriod(group2, new LocalDate(2020, 2, 5), new LocalDate(2015, 2, 10));
        testGroupPeriod(group2, new LocalDate(2020, 2, 5), new LocalDate(2050, 2, 10));
        testGroupPeriod(group3, new LocalDate(2015, 2, 10), new LocalDate(2016, 3, 10));
        testGroupPeriod(group3, new LocalDate(2015, 2, 11), new LocalDate(2015, 2, 11));
        testGroupPeriod(group4, new LocalDate(2020, 2, 10), new LocalDate(2015, 2, 10));
        testGroupPeriod(group4, new LocalDate(2020, 2, 11), new LocalDate(2050, 2, 22));
    }
}
