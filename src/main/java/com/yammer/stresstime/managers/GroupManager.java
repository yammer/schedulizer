package com.yammer.stresstime.managers;

import com.yammer.stresstime.entities.Group;
import org.hibernate.SessionFactory;

public class GroupManager extends EntityManager<Group> {

    public GroupManager(SessionFactory sessionFactory) {
        super(sessionFactory, Group.class);
    }
}
