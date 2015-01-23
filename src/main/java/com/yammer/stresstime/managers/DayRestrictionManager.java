package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.DayRestriction;
import org.hibernate.SessionFactory;

public class DayRestrictionManager extends EntityManager<DayRestriction> {

    public DayRestrictionManager(SessionFactory sessionFactory) {
        super(sessionFactory, DayRestriction.class);
    }
}
