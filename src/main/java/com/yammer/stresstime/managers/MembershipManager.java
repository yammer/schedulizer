package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Membership;
import org.hibernate.SessionFactory;

public class MembershipManager extends EntityManager<Membership> {

    public MembershipManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
