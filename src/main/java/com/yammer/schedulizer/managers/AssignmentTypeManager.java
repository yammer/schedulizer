package com.yammer.schedulizer.managers;

import com.yammer.schedulizer.entities.AssignmentType;
import org.hibernate.SessionFactory;

public class AssignmentTypeManager extends EntityManager<AssignmentType> {

    public AssignmentTypeManager(SessionFactory sessionFactory) {
        super(sessionFactory, AssignmentType.class);
    }
}
