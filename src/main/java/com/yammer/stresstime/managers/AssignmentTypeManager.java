package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.AssignmentType;
import org.hibernate.SessionFactory;

public class AssignmentTypeManager extends EntityManager<AssignmentType> {

    public AssignmentTypeManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
