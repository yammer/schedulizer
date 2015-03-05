package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.*;
import com.yammer.stresstime.fixtures.*;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;

public class AssignableDayManagerTest extends BaseManagerTest<AssignableDay> {

    private AssignableDayManager assignableDayManager;
    private List<Group> groups;
    private List<Employee> employees;
    private List<AssignmentType> assignmentTypes;
    private List<Membership> memberships;
    private List<AssignableDay> testAssignableDays;
    private AssignableDaysFixture assignableDaysFixture;

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
        employees.stream().forEach(e -> refresh(e));
        groups.stream().forEach(g -> refresh(g));
        assignableDaysFixture = new AssignableDaysFixture(groupsFixture);
        testAssignableDays = assignableDaysFixture.getAssignableDays();
    }

    @Override
    protected void clean() {}

    void testPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        List<AssignableDay> found = assignableDayManager.getByGroupPeriod(group, startDate, endDate);
        TestUtils.testAssignableDaysGroupPeriod(group, startDate, endDate, testAssignableDays, found);
    }


    @Test
    public void testGetByGroupPeriod() {
        assignableDaysFixture.save(getSessionFactory());
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
