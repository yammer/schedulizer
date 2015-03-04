package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.AssignableDay;
import com.yammer.stresstime.entities.Assignment;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.fixtures.*;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class AssignmentsResourceTest extends BaseResourceTest {

    private List<Employee> employees;
    private List<Group> groups;
    private List<AssignableDay> assignableDays;
    private List<Assignment> testAssignments;
    private AssignmentsFixture assignmentsFixture;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        groups = groupsFixture.getGroups();
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        membershipsFixture.save(getSessionFactory());
        employees.stream().forEach(e -> refresh(e));
        groups.stream().forEach(g -> refresh(g));
        AssignmentTypesFixture assignmentTypesFixture = new AssignmentTypesFixture(groupsFixture);
        assignmentTypesFixture.save(getSessionFactory());
        AssignableDaysFixture assignableDaysFixture = new AssignableDaysFixture(groupsFixture);
        assignableDaysFixture.save(getSessionFactory());
        assignableDays = assignableDaysFixture.getAssignableDays();
        assignmentsFixture = new AssignmentsFixture(employeesFixture, assignmentTypesFixture, assignableDaysFixture);
        testAssignments = assignmentsFixture.getAssignments();
    }

    private String getAssignmentsPath(Group group, LocalDate startDate, LocalDate endDate) {
        UriBuilder uriBuilder = UriBuilder.fromPath("/groups")
                .path("/{group_id}/assignments")
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString());
        return uriBuilder.build(group.getId()).toString();
    }

    @Test
    public void testCreateAssignments() {
        assignmentsFixture.save(getSessionFactory());
        testAssignments.stream().forEach(assignment -> {
            MultivaluedMapImpl form = new MultivaluedMapImpl();
            form.add("employee_id", assignment.getEmployeeId());
            form.add("assignment_type_id", assignment.getAssignmentTypeId());
            form.add("dates", assignment.getAssignableDay().getDateString());
            String path = String.format("groups/%d/assignments", assignment.getAssignableDay().getGroupId());
            Map<String, Object> created = resource(path).entity(form).post(Map[].class)[0];
            assertNotNull(created);
            assertThat(new Long((Integer)created.get("groupId")), equalTo(assignment.getAssignableDay().getGroupId()));
            assertThat(created.get("date"), equalTo(assignment.getAssignableDay().getDateString()));
            assertTrue(((List)created.get("assignments")).size() > 0);
        });
    }

    private void testGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        String path = getAssignmentsPath(group, startDate, endDate);
        List<AssignableDay> found = Arrays.asList(resource(path).get(AssignableDay[].class));
        TestUtils.testAssignableDaysGroupPeriod(group, startDate, endDate, assignableDays, found);

    }

    @Test
    public void testGetAssignableDays() {
        assignmentsFixture.save(getSessionFactory());
        assignableDays.stream().forEach(a -> refresh(a));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testGroupPeriod(groups.get(0), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testGroupPeriod(groups.get(0), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 3, 31));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testGroupPeriod(groups.get(0), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));

        testGroupPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testGroupPeriod(groups.get(1), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testGroupPeriod(groups.get(1), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 3, 31));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testGroupPeriod(groups.get(1), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));

        testGroupPeriod(groups.get(2), new LocalDate(2015, 1, 10), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 2, 10), new LocalDate(2015, 2, 20));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 2, 15), new LocalDate(2015, 1, 12));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 28));
        testGroupPeriod(groups.get(2), new LocalDate(2016, 2, 10), new LocalDate(2016, 2, 29));
        testGroupPeriod(groups.get(2), new LocalDate(2016, 1, 10), new LocalDate(2016, 3, 1));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 12, 10), new LocalDate(2015, 12, 12));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 1, 10), new LocalDate(2015, 3, 31));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 1, 10), new LocalDate(2015, 2, 12));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 5, 17), new LocalDate(2015, 6, 20));
        testGroupPeriod(groups.get(2), new LocalDate(2014, 12, 31), new LocalDate(2015, 1, 12));
    }

    @Test
    public void deleteAssignableDayTest() {
        assignmentsFixture.save(getSessionFactory());
        Assignment assignment = testAssignments.get(0);
        refresh(assignment);
        String path = getAssignmentsPath(assignment.getAssignableDay().getGroup(),
                assignment.getAssignableDay().getDate(),
                assignment.getAssignableDay().getDate());
        Map<String, Object> found = resource(path).get(Map[].class)[0];
        assertTrue(((List) found.get("assignments")).stream().anyMatch(a -> new Long((Integer) ((Map) a).get("id")) == assignment.getId()));
        String deletePath = String.format("/groups/%d/assignments/%d", assignment.getAssignableDay().getGroupId(), assignment.getId());
        resource(deletePath).delete();
        Map<String, Object> newFound = resource(path).get(Map[].class)[0];
        assertFalse(((List) newFound.get("assignments")).stream().anyMatch(a -> new Long((Integer) ((Map) a).get("id")) == assignment.getId()));
    }
}
