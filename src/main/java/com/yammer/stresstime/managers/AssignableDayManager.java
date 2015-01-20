package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.AssignableDay;
import org.hibernate.SessionFactory;

public class AssignableDayManager extends EntityManager<AssignableDay> {

    public AssignableDayManager(SessionFactory sessionFactory) {
        super(sessionFactory, AssignableDay.class);
    }
}
