package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Assignment;
import org.hibernate.SessionFactory;

public class AssignmentManager extends EntityManager<Assignment> {

    public AssignmentManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
