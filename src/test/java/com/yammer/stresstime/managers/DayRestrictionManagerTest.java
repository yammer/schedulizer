package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.DayRestriction;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.fixtures.DayRestrictionsFixture;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

public class DayRestrictionManagerTest extends BaseManagerTest<DayRestriction> {

    private DayRestrictionManager dayRestrictionManager;
    private List<DayRestriction> testDayRestrictions;
    private List<Employee> employees;
    private DayRestrictionsFixture dayRestrictionsFixture;

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
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        dayRestrictionsFixture = new DayRestrictionsFixture(employeesFixture);
        testDayRestrictions = dayRestrictionsFixture.getDayRestrictions();
    }

    @Override
    protected void clean() {}

    void testEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        List<DayRestriction> found = dayRestrictionManager.getByEmployeePeriod(employee, startDate, endDate);
        TestUtils.testDayRestrictionEmployeePeriod(employee, startDate, endDate, testDayRestrictions, found);
    }

    void testGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        List<DayRestriction> found = dayRestrictionManager.getByGroupPeriod(group, startDate, endDate);
        TestUtils.testDayRestrictionGroupPeriod(group, startDate, endDate, testDayRestrictions, found);
    }

    @Test
    public void testGetByEmployeePeriod() {
        dayRestrictionsFixture.save(getSessionFactory());
        testEmployeePeriod(employees.get(0), new LocalDate(2015, 2, 10), new LocalDate(2015, 3, 10));
        testEmployeePeriod(employees.get(0), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 10));
        testEmployeePeriod(employees.get(0), new LocalDate(2020, 2, 5), new LocalDate(2015, 2, 10));
        testEmployeePeriod(employees.get(0), new LocalDate(2020, 2, 5), new LocalDate(2050, 2, 10));
        testEmployeePeriod(employees.get(1), new LocalDate(2015, 2, 10), new LocalDate(2016, 3, 10));
        testEmployeePeriod(employees.get(1), new LocalDate(2015, 2, 11), new LocalDate(2015, 2, 11));
        testEmployeePeriod(employees.get(2), new LocalDate(2020, 2, 10), new LocalDate(2015, 2, 10));
        testEmployeePeriod(employees.get(2), new LocalDate(2020, 2, 11), new LocalDate(2050, 2, 22));
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
        dayRestrictionsFixture.save(getSessionFactory());
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
