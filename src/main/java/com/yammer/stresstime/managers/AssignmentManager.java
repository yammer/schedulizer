package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Assignment;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

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
}
