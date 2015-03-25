package com.yammer.schedulizer.managers;

import com.google.common.collect.Maps;
import com.yammer.schedulizer.entities.AssignableDay;
import com.yammer.schedulizer.entities.Assignment;
import com.yammer.schedulizer.entities.AssignmentType;
import com.yammer.schedulizer.entities.Employee;
import com.yammer.schedulizer.utils.CoreUtils;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignmentManager extends EntityManager<Assignment> {

    public AssignmentManager(SessionFactory sessionFactory) {
        super(sessionFactory, Assignment.class);
    }

    public Map<Employee, Map<AssignmentType, Long>> getStatistics(
            Collection<? extends AssignableDay> assignableDays) {

        // maps list of assignments per employee from a list of assignableDays
        Map<Employee, List<Assignment>> employeeAssignments = assignableDays.stream()
                .map(day -> day.getAssignments().stream()
                                .collect(Collectors.groupingBy(Assignment::getEmployee)))
                .collect(CoreUtils.mergingMapCollector(HashMap::new, CoreUtils::concatLists));

        // counts how many assignments belong to each assignment type
        return Maps.transformValues(employeeAssignments,
                as -> as.stream()
                        .collect(CoreUtils.countingByCollector(Assignment::getAssignmentType)));
    }

    public boolean exists(Assignment assignment) {
        return !currentSession().createCriteria(Assignment.class)
                .add(Restrictions.eq("employee.id", assignment.getEmployee().getId()))
                .add(Restrictions.eq("assignableDay.id", assignment.getAssignableDay().getId()))
                .add(Restrictions.eq("assignmentType.id", assignment.getAssignmentType().getId()))
                .list()
                .isEmpty();
    }

    public List<Assignment> getByEmployeePeriod(Employee employee, LocalDate startDate, LocalDate endDate) {

        return currentSession()
                .createCriteria(Assignment.class)
                .add(Restrictions.eq("employee.id", employee.getId()))
                .createCriteria("assignableDay")
                .add(Restrictions.ge("date", startDate))
                .add(Restrictions.le("date", endDate))
                .list();
    }
}
