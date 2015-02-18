package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Assignment;
import com.yammer.stresstime.entities.Employee;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import java.util.List;

public class AssignmentManager extends EntityManager<Assignment> {

    public AssignmentManager(SessionFactory sessionFactory) {
        super(sessionFactory, Assignment.class);
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
