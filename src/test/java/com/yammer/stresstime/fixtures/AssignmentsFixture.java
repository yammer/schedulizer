package com.yammer.stresstime.fixtures;

import com.google.common.collect.Lists;
import com.yammer.stresstime.entities.AssignableDay;
import com.yammer.stresstime.entities.Assignment;
import com.yammer.stresstime.entities.AssignmentType;
import com.yammer.stresstime.entities.Employee;
import com.yammer.stresstime.managers.AssignmentManager;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;

public class AssignmentsFixture {

    private List<Assignment> assignments;
    private EmployeesFixture employeesFixture;
    private AssignmentTypesFixture assignmentTypesFixture;
    private AssignableDaysFixture assignableDaysFixture;
    private boolean saved;

    public AssignmentsFixture(EmployeesFixture employeesFixture, AssignmentTypesFixture assignmentTypesFixture, AssignableDaysFixture assignableDaysFixture) {
        this.employeesFixture = employeesFixture;
        this.assignableDaysFixture = assignableDaysFixture;
        this.assignmentTypesFixture = assignmentTypesFixture;
        saved = false;
        List<Employee> employees = employeesFixture.getEmployees();
        List<AssignmentType> assignmentTypes = assignmentTypesFixture.getAssignmentTypes();
        List<AssignableDay> assignableDays = assignableDaysFixture.getAssignableDays();
        assignments = Lists.newArrayList(
                new Assignment(employees.get(0), assignableDays.get(0), assignmentTypes.get(0)),
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

    public void save(SessionFactory sessionFactory) {
        if (saved) return;
        saved = true;
        employeesFixture.save(sessionFactory);
        assignmentTypesFixture.save(sessionFactory);
        assignableDaysFixture.save(sessionFactory);
        AssignmentManager assignmentManager = new AssignmentManager(sessionFactory);
        assignments.stream().forEach(e -> assignmentManager.save(e));
    }

    public void checkStatistics(Map<Employee, Map<AssignmentType, Long>> statistics) {
        List<Employee> employees = employeesFixture.getEmployees();
        List<AssignmentType> assignmentTypes = assignmentTypesFixture.getAssignmentTypes();

        assertThat(statistics.get(employees.get(0)).get(assignmentTypes.get(0)), equalTo(3L));
        assertThat(statistics.get(employees.get(0)).get(assignmentTypes.get(1)), equalTo(2L));
        assertThat(statistics.get(employees.get(0)).get(assignmentTypes.get(2)), equalTo(3L));
        assertNull(statistics.get(employees.get(0)).get(assignmentTypes.get(3)));
        assertNull(statistics.get(employees.get(0)).get(assignmentTypes.get(4)));
        assertNull(statistics.get(employees.get(0)).get(assignmentTypes.get(5)));
        assertNull(statistics.get(employees.get(0)).get(assignmentTypes.get(6)));
        assertThat(statistics.get(employees.get(0)).get(assignmentTypes.get(7)), equalTo(1L));

        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(0)), equalTo(2L));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(1)), equalTo(2L));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(2)), equalTo(3L));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(3)), equalTo(2L));
        assertNull(statistics.get(employees.get(1)).get(assignmentTypes.get(4)));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(5)), equalTo(3L));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(6)), equalTo(1L));
        assertThat(statistics.get(employees.get(1)).get(assignmentTypes.get(7)), equalTo(3L));

        assertNull(statistics.get(employees.get(2)).get(assignmentTypes.get(0)));
        assertNull(statistics.get(employees.get(2)).get(assignmentTypes.get(1)));
        assertNull(statistics.get(employees.get(2)).get(assignmentTypes.get(2)));
        assertThat(statistics.get(employees.get(2)).get(assignmentTypes.get(3)), equalTo(3L));
        assertThat(statistics.get(employees.get(2)).get(assignmentTypes.get(4)), equalTo(2L));
        assertThat(statistics.get(employees.get(2)).get(assignmentTypes.get(5)), equalTo(1L));
        assertThat(statistics.get(employees.get(2)).get(assignmentTypes.get(6)), equalTo(3L));
        assertThat(statistics.get(employees.get(2)).get(assignmentTypes.get(7)), equalTo(4L));
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}
