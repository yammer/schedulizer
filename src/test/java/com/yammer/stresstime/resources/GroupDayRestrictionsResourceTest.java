package com.yammer.stresstime.resources;

import com.sun.jersey.api.uri.UriBuilderImpl;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yammer.stresstime.entities.DayRestriction;
import com.yammer.stresstime.entities.Group;
import com.yammer.stresstime.entities.Membership;
import com.yammer.stresstime.fixtures.DayRestrictionsFixture;
import com.yammer.stresstime.fixtures.EmployeesFixture;
import com.yammer.stresstime.fixtures.GroupsFixture;
import com.yammer.stresstime.fixtures.MembershipsFixture;
import com.yammer.stresstime.managers.DayRestrictionManager;
import com.yammer.stresstime.test.TestUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class GroupDayRestrictionsResourceTest extends BaseResourceTest {

    private List<Group> groups;
    private List<DayRestriction> dayRestrictions;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        EmployeesFixture employeesFixture = new EmployeesFixture();
        employeesFixture.save(getSessionFactory());
        GroupsFixture groupsFixture = new GroupsFixture();
        groupsFixture.save(getSessionFactory());
        MembershipsFixture membershipsFixture = new MembershipsFixture(employeesFixture, groupsFixture);
        membershipsFixture.save(getSessionFactory());
        groups = groupsFixture.getGroups();
        groups.stream().forEach(g -> refresh(g));
        employeesFixture.getEmployees().stream().forEach(e -> refresh(e));

        DayRestrictionsFixture dayRestrictionsFixture = new DayRestrictionsFixture(employeesFixture);
        dayRestrictionsFixture.save(getSessionFactory());
        dayRestrictions = dayRestrictionsFixture.getDayRestrictions();
    }

    private void testGroupPeriod(Group group, LocalDate startDate, LocalDate endDate) {
        UriBuilder uriBuilder = UriBuilder.fromPath("/groups")
                .path("/{group_id}/restrictions")
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString());
        String path = uriBuilder.build(group.getId()).toString();
        List<DayRestriction> found = Arrays.asList(resource(path).get(DayRestriction[].class));
        List<Map<String, String>> response = Arrays.asList(resource(path).get(Map[].class));
        response.stream().forEach(r -> {
            assertNotNull(r.get("restrictionLevel"));
            assertNotNull(r.get("employeeId"));
            assertNotNull(r.get("date"));
        });
        TestUtils.testDayRestrictionGroupPeriod(group, startDate, endDate, dayRestrictions, found);
    }

    @Test
    public void testGetGroupRestrictions() {
        testGroupPeriod(groups.get(0), new LocalDate(2015, 2, 10), new LocalDate(2015, 3, 10));
        testGroupPeriod(groups.get(0), new LocalDate(2015, 2, 5), new LocalDate(2015, 2, 10));
        testGroupPeriod(groups.get(1), new LocalDate(2020, 2, 5), new LocalDate(2015, 2, 10));
        testGroupPeriod(groups.get(1), new LocalDate(2020, 2, 5), new LocalDate(2050, 2, 10));
        testGroupPeriod(groups.get(1), new LocalDate(2015, 2, 10), new LocalDate(2016, 3, 10));
        testGroupPeriod(groups.get(2), new LocalDate(2015, 2, 11), new LocalDate(2015, 2, 11));
        testGroupPeriod(groups.get(2), new LocalDate(2020, 2, 10), new LocalDate(2015, 2, 10));
        testGroupPeriod(groups.get(2), new LocalDate(2020, 2, 11), new LocalDate(2050, 2, 22));
    }
}
