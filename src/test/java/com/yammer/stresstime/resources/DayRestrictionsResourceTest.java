package com.yammer.stresstime.resources;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.DayRestriction;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.fixtures.DayRestrictionsFixture;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.managers.DayRestrictionManager;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class DayRestrictionsResourceTest extends BaseResourceTest{

    private List<Employee> employees;
    private List<DayRestriction> testDayRestrictions;
    private DayRestrictionsFixture dayRestrictionsFixture;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        employees = employeesFixture.getEmployees();
        dayRestrictionsFixture = new DayRestrictionsFixture(employeesFixture);
        testDayRestrictions = dayRestrictionsFixture.getDayRestrictions();
        setCurrentUser(getGlobalAdmin());
    }

    private String getDayRestrictionsPath(Employee employee, LocalDate startDate, LocalDate endDate) {
        UriBuilder uriBuilder = UriBuilder.fromPath("/employees")
                .path("/{employee_id}/restrictions")
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString());
        return uriBuilder.build(employee.getId()).toString();
    }

    private void testEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        UriBuilder uriBuilder = UriBuilder.fromPath("/employees")
                .path("/{employee_id}/restrictions")
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString());
        String path = getDayRestrictionsPath(employee, startDate, endDate);
        List<DayRestriction> found = Arrays.asList(resource(path).get(DayRestriction[].class));
        List<Map<String, String>> response = Arrays.asList(resource(path).get(Map[].class));
        response.stream().forEach(r -> {
            assertNotNull(r.get("restrictionLevel"));
            assertNotNull(r.get("employeeId"));
            assertNotNull(r.get("date"));
        });
        TestUtils.testDayRestrictionEmployeePeriod(employee, startDate, endDate, testDayRestrictions, found);
    }

    private void testPeriods() {
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
    public void testGet() {
        dayRestrictionsFixture.save(getSessionFactory());
        testPeriods();
    }

    @Test
    public void testCreate() {
        testDayRestrictions.stream().forEach(d -> {
            setCurrentUser(d.getEmployee());
            MultivaluedMapImpl form = new MultivaluedMapImpl();
            form.add("dates", d.getDateString());
            d.setComment(TestUtils.nextRandomString());
            form.add("comment", d.getComment());
            form.add("restriction_level", d.getRestrictionLevel());
            DayRestriction retrieved = resource(String.format("employees/%d/restrictions", d.getEmployee().getId()))
                    .entity(form)
                    .post(DayRestriction[].class)[0];
            String rr = resource(String.format("employees/%d/restrictions", d.getEmployee().getId()))
                    .entity(form)
                    .post(String.class);
            assertNotNull(retrieved);
            assertThat(retrieved.getDateString(), equalTo(d.getDateString()));
            assertThat(retrieved.getRestrictionLevel(), equalTo(d.getRestrictionLevel()));
            assertThat(retrieved.getComment(), equalTo(d.getComment()));
        });
    }

    @Test
    public void testDelete() {
        dayRestrictionsFixture.save(getSessionFactory());
        String path0 = getDayRestrictionsPath(
                testDayRestrictions.get(0).getEmployee(),
                testDayRestrictions.get(0).getDate(),
                testDayRestrictions.get(0).getDate());
        DayRestriction dayRestriction = resource(path0).get(DayRestriction[].class)[0];
        assertNotNull(dayRestriction);
        assertThat(dayRestriction, equalTo(testDayRestrictions.get(0)));
        String path1 = String.format("/employees/%d/restrictions/%d",
                testDayRestrictions.get(0).getEmployeeId(),
                testDayRestrictions.get(0).getId());
        setCurrentUser(testDayRestrictions.get(0).getEmployee());
        resource(path1).delete();
        setCurrentUser(getGlobalAdmin());
        String path2 = getDayRestrictionsPath(
                testDayRestrictions.get(0).getEmployee(),
                testDayRestrictions.get(0).getDate(),
                testDayRestrictions.get(0).getDate());
        List<DayRestriction> dayRestrictions = Arrays.asList(resource(path0).get(DayRestriction[].class));
        assertNotNull(dayRestrictions);
        assertThat(dayRestrictions.size(), equalTo(0));
    }
}
